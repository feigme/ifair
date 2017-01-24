package com.ifair.oauth2.oltu.service;

import com.ifair.oauth2.oltu.domain.OauthAuthorizeDO;
import com.ifair.oauth2.oltu.domain.OauthClientDO;
import com.ifair.uic.domain.UserDO;

/**
 * Created by feiying on 16/9/23.
 */
public interface OauthClientService {

	/**
	 * 查询客户信息
	 * 
	 * @param clientId
	 * @return
	 */
	OauthClientDO findClientByClientId(String clientId);

	/**
	 * 用户是否已经给这个客户授权
	 */
	OauthAuthorizeDO findAuthorize(OauthClientDO client, UserDO user);

	UserDO loginCheck(String userName, String password);

	boolean authorize(OauthClientDO client, UserDO user);

	void put(String key, Object value);

	Object get(String key);

	void evict(String key);

}
