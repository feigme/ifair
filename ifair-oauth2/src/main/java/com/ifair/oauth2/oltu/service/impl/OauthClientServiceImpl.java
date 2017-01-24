package com.ifair.oauth2.oltu.service.impl;

import com.google.common.collect.Maps;
import com.ifair.common.security.CommonUsage;
import com.ifair.common.security.DesCbcSecurity;
import com.ifair.oauth2.oltu.domain.OauthAuthorizeDO;
import com.ifair.oauth2.oltu.domain.OauthClientDO;
import com.ifair.oauth2.oltu.mapper.OauthAuthorizeDOMapper;
import com.ifair.oauth2.oltu.mapper.OauthClientDOMapper;
import com.ifair.oauth2.oltu.service.OauthClientService;
import com.ifair.uic.domain.UserDO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by feiying on 17/1/24.
 */
@Service
public class OauthClientServiceImpl implements OauthClientService {

	private static Map<String, Object> cache = Maps.newHashMap();

	@Resource
	private OauthClientDOMapper oauthClientDOMapper;
	@Resource
	private OauthAuthorizeDOMapper oauthAuthorizeDOMapper;

	@Override
	public OauthClientDO findClientByClientId(String clientId) {
		return oauthClientDOMapper.findClientByClientId(clientId);
	}

	@Override
	public OauthAuthorizeDO findAuthorize(OauthClientDO client, UserDO user) {
		return oauthAuthorizeDOMapper.findOauthAuthorize(user.getId(), client.getId());
	}

	@Override
	public boolean authorize(OauthClientDO client, UserDO user) {
		OauthAuthorizeDO oauthAuthorizeDO = new OauthAuthorizeDO();
		oauthAuthorizeDO.setUserId(user.getId());
		oauthAuthorizeDO.setOauthClientId(client.getId());
		return oauthAuthorizeDOMapper.insertSelective(oauthAuthorizeDO) == 1;
	}

	@Override
	public void put(String key, Object value) {
		cache.put(key, value);
	}

	@Override
	public Object get(String key) {
		return cache.get(key);
	}

	@Override
	public void evict(String key) {
		cache.remove(key);
	}
}
