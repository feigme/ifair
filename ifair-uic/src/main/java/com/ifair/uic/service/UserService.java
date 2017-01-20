package com.ifair.uic.service;

import com.ifair.base.BizResult;
import com.ifair.uic.domain.UserDO;

import java.util.List;

/**
 * Created by feiying on 17/1/18.
 */
public interface UserService {

	/**
	 * 注册
	 * 
	 * @param userDO
	 * @return
	 */
	BizResult<Long> register(UserDO userDO);

	/**
	 * 根据电话查找用户
	 * 
	 * @param mobile
	 * @return
	 */
	BizResult<List<UserDO>> findUserByMobile(String mobile);

}
