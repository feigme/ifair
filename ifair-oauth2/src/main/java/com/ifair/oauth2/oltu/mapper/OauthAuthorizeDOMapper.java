package com.ifair.oauth2.oltu.mapper;

import com.ifair.oauth2.oltu.domain.OauthAuthorizeDO;
import org.apache.ibatis.annotations.Param;

public interface OauthAuthorizeDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(OauthAuthorizeDO record);

    int insertSelective(OauthAuthorizeDO record);

    OauthAuthorizeDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(OauthAuthorizeDO record);

    int updateByPrimaryKey(OauthAuthorizeDO record);

    OauthAuthorizeDO findOauthAuthorize(@Param("userId") Long userId, @Param("clientId") Long clientId);
}