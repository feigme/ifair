package com.ifair.uic.mapper;

import com.ifair.uic.domain.UserDO;

public interface UserDOMapper {
	int deleteByPrimaryKey(Long id);

	int insert(UserDO record);

	int insertSelective(UserDO record);

	UserDO selectByPrimaryKey(Long id);

	int updateByPrimaryKeySelective(UserDO record);

	int updateByPrimaryKey(UserDO record);

	UserDO findUserByMobile(String mobile);
}