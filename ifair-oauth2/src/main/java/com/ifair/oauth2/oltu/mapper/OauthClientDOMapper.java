package com.ifair.oauth2.oltu.mapper;

import com.ifair.oauth2.oltu.domain.OauthClientDO;

public interface OauthClientDOMapper {
	int deleteByPrimaryKey(Long id);

	int insert(OauthClientDO record);

	int insertSelective(OauthClientDO record);

	OauthClientDO selectByPrimaryKey(Long id);

	int updateByPrimaryKeySelective(OauthClientDO record);

	int updateByPrimaryKey(OauthClientDO record);

	OauthClientDO findClientByClientId(String clientId);
}