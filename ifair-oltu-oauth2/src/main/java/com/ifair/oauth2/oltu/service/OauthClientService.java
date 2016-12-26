package com.ifair.oauth2.oltu.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ifair.oauth2.oltu.model.OauthAuthorize;
import com.ifair.oauth2.oltu.model.OauthClient;
import com.ifair.oauth2.oltu.model.OauthUser;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by feiying on 16/9/23.
 */
public class OauthClientService {

	private static List<OauthClient> clientList = Lists.newArrayList();
	private static List<OauthAuthorize> authorizeList = Lists.newArrayList();
	private static List<OauthUser> userList = Lists.newArrayList();

	private static Map<String, Object> cache = Maps.newHashMap();

	static {
		clientList.add(new OauthClient(1L, "client_1", "aaabbb", "斯柯达", "code", "http://www.baidu.com", null));
		clientList.add(new OauthClient(2L, "client_2", "bbbccc", "上岛咖啡", "code", "http://www.taobao.com", null));

		userList.add(new OauthUser(1L, "test", "123456"));
		userList.add(new OauthUser(2L, "aaa", "123456"));
	}

	/**
	 * 查询客户信息
	 * 
	 * @param clientId
	 * @return
	 */
	public OauthClient findByClientId(String clientId) {
		for (OauthClient client : clientList) {
			if (StringUtils.equals(clientId, client.getClientId())) {
				return client;
			}
		}
		return null;
	}

	/**
	 * 用户是否已经给这个客户授权
	 */
	public OauthAuthorize findAuthorize(OauthClient client, OauthUser user) {
		if (client == null || user == null) {
			return null;
		}
		for (OauthAuthorize authorize : authorizeList) {
			if (authorize.getClientId() == client.getId() && authorize.getUserId() == user.getId()) {
				return authorize;
			}
		}
		return null;
	}

	public OauthUser loginCheck(String userName, String password) {
		for (OauthUser user: userList) {
			if (user.getUserName().equals(userName) && user.getPassword().equals(password)){
				return user;
			}
		}
		return null;
	}

	public boolean authorize(OauthClient client, OauthUser user) {
		authorizeList.add(new OauthAuthorize(client.getId(), user.getId()));
		return true;
	}

	public void put(String key, Object value) {
		cache.put(key, value);
	}

	public Object get(String key) {
		return cache.get(key);
	}

	public void evict(String key) {
		cache.remove(key);
	}

}
