package com.ifair.uic.service.impl;

import com.ifair.base.BizResult;
import com.ifair.common.security.CommonUsage;
import com.ifair.common.security.DesCbcSecurity;
import com.ifair.uic.domain.UserDO;
import com.ifair.uic.mapper.UserDOMapper;
import com.ifair.uic.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by feiying on 17/1/18.
 */
@Service
public class UserServiceImpl implements UserService {

	public static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

	@Resource
	private UserDOMapper userDOMapper;

	@Override
	public BizResult<Long> register(UserDO userDO) {
		try {
			String salt = CommonUsage.generateSlat(6);
			String encrptPassword = (DesCbcSecurity.encode(DesCbcSecurity.md5(userDO.getPassword()) + DesCbcSecurity.md5(salt))).toUpperCase();

			userDO.setSalt(salt);
			userDO.setPassword(encrptPassword);

			userDOMapper.insertSelective(userDO);
			if (userDO.getId() != null) {
				return new BizResult<>(true).setData(userDO.getId());
			}
			return new BizResult<>(false).setMessage("注册失败");
		} catch (Exception e) {
			log.error("register error", e);
			return new BizResult<>(false).setMessage("系统异常");
		}
	}

	@Override
	public BizResult<UserDO> findUserByMobile(String mobile) {
		UserDO userDO = userDOMapper.findUserByMobile(mobile);
		return new BizResult<>(true).setData(userDO);
	}

	@Override
	public BizResult<UserDO> findUserById(Long id) {
		return new BizResult<>(true).setData(userDOMapper.selectByPrimaryKey(id));
	}

	@Override
	public BizResult<UserDO> checkPassword(String mobile, String password) {
		UserDO userDO = userDOMapper.findUserByMobile(mobile);
		if (userDO == null) {
			return new BizResult<>(false).setMessage("手机号码不存在!");
		}

		try {
			String salt = CommonUsage.generateSlat(6);
			String encrptPassword = (DesCbcSecurity.encode(DesCbcSecurity.md5(userDO.getPassword()) + DesCbcSecurity.md5(salt))).toUpperCase();
			if (StringUtils.equals(encrptPassword, password)) {
				return new BizResult<>(true).setData(userDO);
			}
			return new BizResult<>(false).setMessage("密码错误!");
		} catch (Exception e) {
			log.error("checkPassword error", e);
			return new BizResult<>(false).setMessage("系统异常");
		}
	}
}
