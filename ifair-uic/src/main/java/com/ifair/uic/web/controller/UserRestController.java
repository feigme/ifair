package com.ifair.uic.web.controller;

import com.alibaba.fastjson.JSON;
import com.ifair.base.BizResult;
import com.ifair.uic.domain.UserDO;
import com.ifair.uic.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by feiying on 17/1/18.
 */
@Controller
@RequestMapping("/rest/uic")
public class UserRestController {

	@Resource
	private UserService userService;

	@RequestMapping(value = "/register", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String register(UserDO userDO) {
		BizResult<Long> bizResult = new BizResult<>(false);

		if (StringUtils.isEmpty(userDO.getMobile())) {
			return JSON.toJSONString(bizResult.setMessage("手机号码为空!"));
		}

		if (StringUtils.isEmpty(userDO.getPassword())) {
			return JSON.toJSONString(bizResult.setMessage("密码为空!"));
		}

		BizResult<List<UserDO>> listBizResult = userService.findUserByMobile(userDO.getMobile());
		if (listBizResult.getSuccess() && listBizResult.getData().size() > 0) {
			return JSON.toJSONString(bizResult.setMessage("手机号码已被使用!"));
		}

		bizResult = userService.register(userDO);
		return JSON.toJSONString(bizResult);
	}

}
