package com.ifair.common.interceptor.mybatis;

import org.apache.ibatis.executor.BaseExecutor;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.DefaultParameterHandler;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Intercepts({ @Signature(method = "query", type = Executor.class, args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }) })
public class PagingInterceptor implements Interceptor {
	private static Logger log = LoggerFactory.getLogger(PagingInterceptor.class);
	private ConcurrentMap<String, Boolean> ignoreMap = new ConcurrentHashMap<>();

	/**
	 * 拦截后要执行的方法
	 */
	public Object intercept(Invocation invocation) throws Throwable {
		Object[] args = invocation.getArgs();
		PagingDevice page = getPagingDevice(args[1]);
		if (page != null && isNeedPaging((MappedStatement) args[0], page)) {
			MappedStatement mappedStatement = (MappedStatement) args[0];
			Connection connection = getConnection(invocation);
			// 给当前的page参数对象设置总记录数
			int count = getTotalCount((Executor) invocation.getTarget(), args, page, mappedStatement, connection);
			page.setCount(count);
			if (page.getCurrentPage() > page.getTotalPage() - 1) {
				page.setCurrentPage(page.getLastPage());
			}
			// 替换原有
			SqlSource sqlSource = mappedStatement.getSqlSource();
			if (!(sqlSource instanceof Proxy)) {
				MetaObject.forObject(mappedStatement).setValue("sqlSource", proxySqlSource(sqlSource));
			}
			return new PagingList((List) invocation.proceed(), count, page);
		} else {
			return invocation.proceed();
		}
	}

	private boolean isNeedPaging(MappedStatement mappedStatement, PagingDevice page) {
		if (isNotNeedPaging(page)) {
			return false;
		}
		Boolean res = null;
		String id = mappedStatement.getId();
		try {
			res = ignoreMap.get(id);
			if (res != null) {
				return res;
			}
			String className = id.substring(id.indexOf("com."), id.lastIndexOf("."));
			String methodName = id.substring(id.lastIndexOf(".") + 1);
			Class cla = this.getClass().getClassLoader().loadClass(className);
			Annotation[] annotations = new Annotation[0];
			Class paramClass = page.getClass();
			while (true) {
				try {
					annotations = cla.getMethod(methodName, paramClass).getAnnotations();
					break;
				} catch (Exception e) {
					paramClass = paramClass.getSuperclass();
					if (paramClass == Object.class) {
						throw new Exception("找不到" + methodName + "(" + page.getClass() + ")方法");
					}
				}
			}
			for (Annotation annotation : annotations) {
				if (annotation instanceof IgnorePaging) {
					res = false;
					break;
				}
			}
			if (res == null) {
				res = true;
			}
		} catch (Exception e) {
			res = true;
			log.error("", e);
		}
		ignoreMap.put(id, res);
		return res;
	}

	private boolean isNotNeedPaging(PagingDevice page) {
		return page.getCount() < 0 || page.getPageSize() < 0;
	}

	private PagingDevice getPagingDevice(Object arg) {
		if (arg != null) {
			if (arg instanceof PagingDevice) {
				return (PagingDevice) arg;
			} else if (arg instanceof Map) {
				Map map = (Map) arg;
				Object temp = null;
				for (Object o : map.values()) {
					if (temp == null) {
						temp = o;
					}
					if (temp != o) {
						return null;
					}
				}
				if (temp instanceof PagingDevice) {
					return (PagingDevice) temp;
				}
			}
		}
		return null;
	}

	private SqlSource proxySqlSource(final SqlSource sqlSource) {
		return (SqlSource) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { SqlSource.class }, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				Object o = method.invoke(sqlSource, args);
				if (args != null && args.length == 1) {
					PagingDevice pagingDevice = getPagingDevice(args[0]);
					if (pagingDevice != null && !isNotNeedPaging(pagingDevice)) {
						BoundSql boundSql = (BoundSql) o;
						MetaObject.forObject(boundSql).setValue("sql", getPageSql(pagingDevice, getSql(boundSql)));
					}
				}
				return o;
			}
		});
	}

	/**
	 * 拦截器对应的封装原始对象的方法
	 */
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	/**
	 * 设置注册拦截器时设定的属性
	 */
	public void setProperties(Properties properties) {
	}

	/**
	 * 根据page对象获取对应的分页查询Sql语句，这里只做了两种数据库类型，Mysql和Oracle 其它的数据库都 没有进行分页
	 *
	 * @param page
	 *            分页对象
	 * @param sql
	 *            原sql语句
	 * @return
	 */
	private String getPageSql(PagingDevice page, String sql) {
		StringBuffer sqlBuffer = new StringBuffer(sql);
		return getMysqlPageSql(page, sqlBuffer);
	}

	/**
	 * 获取Mysql数据库的分页查询语句
	 *
	 * @param page
	 *            分页对象
	 * @param sqlBuffer
	 *            包含原sql语句的StringBuffer对象
	 * @return Mysql数据库分页语句
	 */
	private String getMysqlPageSql(PagingDevice page, StringBuffer sqlBuffer) {
		// 计算第一条记录的位置，Mysql中记录的位置是从0开始的。
		int offset = page.getOffset();
		sqlBuffer.append(" limit ").append(offset).append(",").append(page.getPageSize());
		return sqlBuffer.toString();
	}

	/**
	 * 给当前的参数对象page设置总记录数
	 *
	 * @param page
	 *            Mapper映射语句对应的参数对象
	 * @param mappedStatement
	 *            Mapper映射语句
	 * @param connection
	 *            当前的数据库连接
	 */
	private int getTotalCount(Executor executor, Object[] args, PagingDevice page, MappedStatement mappedStatement, Connection connection) {
		// 获取对应的BoundSql，这个BoundSql其实跟我们利用StatementHandler获取到的BoundSql是同一个对象。
		// delegate里面的boundSql也是通过mappedStatement.getBoundSql(paramObj)方法获取到的。
		int totalRecord = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String countSql = null, oldSql = null;
		try {
			int pageSize = page.getPageSize();
			int curentPage = page.getCurrentPage();
			Object sqlParam = args[1];
			BoundSql boundSql;
			try {
				page.setPageSize(Integer.MAX_VALUE);
				page.setCurrentPage(0);
				boundSql = mappedStatement.getBoundSql(sqlParam);
				// 获取真实的最后去执行的sql来统计数量.(可能会有其他的拦截器修改sql)
				Configuration configuration = mappedStatement.getConfiguration();
				StatementHandler handler = configuration.newStatementHandler(executor, mappedStatement, sqlParam, (RowBounds) args[2], (ResultHandler) args[3], boundSql);
				handler.prepare(connection);
				boundSql = handler.getBoundSql();
			} finally {
				page.setPageSize(pageSize);
				page.setCurrentPage(curentPage);
			}
			// 获取到我们自己写在Mapper映射语句中对应的Sql语句
			String sql = getSql(boundSql);
			// 通过查询Sql语句获取到对应的计算总记录数的sql语句
			countSql = getCountSql(sql);
			// 通过BoundSql获取对应的参数映射
			MetaObject metaObject = MetaObject.forObject(boundSql);
			oldSql = boundSql.getSql();
			metaObject.setValue("sql", countSql);
			// 通过mappedStatement、参数对象page和BoundSql对象countBoundSql建立一个用于设定参数的ParameterHandler对象
			ParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, sqlParam, boundSql);
			// 通过connection建立一个countSql对应的PreparedStatement对象。

			pstmt = connection.prepareStatement(countSql);
			// 通过parameterHandler给PreparedStatement对象设置参数
			parameterHandler.setParameters(pstmt);
			// 之后就是执行获取总记录数的Sql语句和获取结果了。
			rs = pstmt.executeQuery();
			if (rs.next()) {
				totalRecord = rs.getInt(1);
			}
		} catch (Exception e) {
			log.warn("分页插件总数统计出错:" + oldSql + "\n" + countSql, e);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
			} catch (SQLException e) {
				log.warn("", e);
			}
		}
		return totalRecord;
	}

	/**
	 * 根据原Sql语句获取对应的查询总记录数的Sql语句
	 *
	 * @param sql
	 * @return
	 */
	private static String getCountSql(String sql) {
		String trimSql = sql.trim();
		if (trimSql.toUpperCase().contains("GROUP")) {
			return "select count(1)  from (" + trimSql + ") as pagingCountTemp";
		} else if (!trimSql.toUpperCase().contains("DISTINCT")) {
			// 备用方案,替换 select 和from 之间的列名和 去掉最后的 order by
			String countSql = "SELECT count(*) ";
			String tempSql = trimSql.substring(trimSql.toUpperCase().indexOf("SELECT") + 6);
			tempSql = tempSql.replace("\t", " ").replace("\r", " ").replace("\n", " ");
			do {
				int index_start = tempSql.toUpperCase().indexOf("SELECT ");
				int fromIndex = tempSql.toUpperCase().indexOf(" FROM ");
				if (index_start > -1 && index_start < fromIndex) {
					// int index_end = tempSql.toUpperCase().indexOf(" FROM ");
					// tempSql = tempSql.substring(index_end);
					tempSql = tempSql.substring(fromIndex + 6);
				} else {
					if (fromIndex > 0) {
						tempSql = tempSql.substring(fromIndex);
					}
					break;
				}
			} while (true);

			int i = tempSql.toUpperCase().lastIndexOf(" ORDER");
			if (i > 0) {
				String s = tempSql.substring(i + 6);
				if (s.trim().toUpperCase().startsWith("BY") && !s.contains("?")) {
					tempSql = tempSql.substring(0, i);
				}
			}
			return countSql + tempSql;
		}
		trimSql = trimSql.substring(6);
		trimSql = "select count(1), " + trimSql;
		return trimSql;
	}

	public static void main(String[] args) {
		System.out.println(getCountSql("select 1,3,(select 2 from 22) as 'dd',4 from daas as od "));
		System.out.println(getCountSql("select 1,3, as 'dd',4 from daas as od "));
		System.out.println(getCountSql(
				"SELECT         t.item_id,         t.shop_id,         ss.shop_name,         t.price,         t.mkt_price,         sk.sku_id,         t.is_deleted,         sic.sold_quantity,         hi.min_order,         hi.store_alert,         hi.runtime_status,         sis.approve_status,         (CASE WHEN t.examine = 0 AND (t.examine_reason = '' OR t.examine_reason IS NULL )         THEN 0         WHEN t.examine = 0 AND t.examine_reason IS NOT NULL AND t.examine_reason != ''         THEN 1         ELSE         2         END) AS examine,         t.examine_reason,         hsd.dispatch_state_id,         hsd.dispatch_city_id,         hsd.dispatch_district_id,         il.id,         il.title,         il.cat_id,         il.barcode,         il.spec,         il.origin,         il.brand,         il.brand_id,         il.image_id,         il.unit,         il.status AS lib_status,         il.type,         il.retail_price,         il.admin_id,         il.shop_id AS add_shop_id,         il.created_time,         il.last_modify_time,         il.comment,         ii.l_url,         ii.m_url,         ii.s_url,         sc.cat_name         FROM         sysitem_item t         LEFT JOIN hyd_item hi ON hi.item_id = t.item_id         LEFT JOIN sysshop_shop ss ON t.shop_id = ss.shop_id         LEFT JOIN sysitem_sku sk ON t.item_id = sk.item_id         LEFT JOIN huigujia_item_lib il ON il.barcode = t.bn         LEFT JOIN image_image ii ON ii.image_id = il.image_id         LEFT JOIN syscategory_cat sc ON sc.cat_id = il.cat_id         LEFT JOIN sysitem_item_count sic ON t.item_id = sic.item_id         LEFT JOIN sysitem_item_status sis ON sis.item_id = t.item_id         LEFT JOIN hyd_supplier_dispatch hsd ON hsd.shop_id = t.shop_id         WHERE 1 = 1         AND il.id IS NOT NULL         AND hi.id IS NOT NULL                       AND t.is_deleted = ?                                 AND t.shop_id =?                                                                                                                                                                                                                                                                                     GROUP BY t.item_id                       ORDER BY t.item_id desc"));
		System.out.println(getCountSql(
				"SELECT d.id AS 'id', d.name AS 'name', d.type AS 'type', d.start_at AS 'startAt', d.end_at AS 'endAt', d.discount_price AS 'discountPrice', d.discount_gift AS 'discountGift', d.ref_type AS 'refType', d.ref_id AS 'refId', d.created_by AS 'createdBy', d.detail AS 'detail', d.examined_status AS 'examinedStatus', CASE WHEN now() >= d.start_at AND d.end_at > now() THEN 'true' ELSE 'false' END AS 'currentActive' FROM huigujia_discount AS d WHERE d.is_deleted = 'false' and d.type = ? and d.created_by = ? order by startAt desc limit 0,20"));
		System.out.println(getCountSql("SELECT\n" + "            sysshop_shop.shop_type,\n" + "            sysitem_item.item_id,\n" + "            sysitem_item.shop_id,\n" + "            title,\n" + "            sub_title,\n"
				+ "            price                                                                        AS item_price,\n" + "            gift_certificate,\n" + "            image_default_id,\n" + "            sysitem_item.cat_id,\n" + "            mkt_price,\n" + "            l_url,\n"
				+ "            m_url,\n" + "            s_url,\n" + "            sold_quantity,\n" + "            sysitem_item_store.store,\n" + "            sysitem_item_store.freez,\n" + "            violation,\n" + "            disabled,\n" + "            examine,\n"
				+ "            sysitem_item.start_date,\n" + "            sysitem_item.end_date,\n" + "            approve_status,\n" + "            ifnull(item_type, 'common')                                                  AS item_type,\n" + "            sysitem_item.remark,\n"
				+ "            (SELECT min(price)\n" + "             FROM sysitem_sku sku, sysitem_sku_store sss\n" + "             WHERE sku.item_id = sysitem_item.item_id AND sku.sku_id = sss.sku_id AND (sss.store - sss.freez) >\n"
				+ "                                                                                      0) AS price\n" + "        FROM sysitem_item\n" + "            JOIN sysshop_shop ON sysitem_item.shop_id = sysshop_shop.shop_id\n"
				+ "            JOIN huigujia_item_tag ON huigujia_item_tag.item_id = sysitem_item.item_id\n" + "            LEFT JOIN image_image ON image_default_id = image_id\n" + "            LEFT JOIN sysitem_item_count ON sysitem_item.item_id = sysitem_item_count.item_id\n"
				+ "            LEFT JOIN sysitem_item_store ON sysitem_item.item_id = sysitem_item_store.item_id\n" + "            LEFT JOIN sysitem_item_status ON sysitem_item.item_id = sysitem_item_status.item_id"));
	}

	private String getSql(BoundSql boundSql) {
		String sql = boundSql.getSql().trim();
		if (sql.endsWith(";")) {
			sql = sql.substring(0, sql.length() - 1);
		}
		return sql;
	}

	private Connection getConnection(Invocation invocation) throws SQLException {
		Object target = invocation.getTarget();
		if (target instanceof CachingExecutor) {
			return ((CachingExecutor) target).getTransaction().getConnection();
		} else if (target instanceof BaseExecutor) {
			return ((BaseExecutor) target).getTransaction().getConnection();
		} else {
			throw new SQLException("找不到 Executor 对象!!! :(");
		}
	}
}
