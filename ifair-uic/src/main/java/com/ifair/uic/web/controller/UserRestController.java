package com.ifair.uic.web.controller;

import com.alibaba.fastjson.JSON;
import com.ifair.base.BizResult;
import com.ifair.uic.domain.UserDO;
import com.ifair.uic.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public String register(@RequestBody UserDO userDO) {
		BizResult<Long> bizResult = new BizResult<>(false);

		if (StringUtils.isEmpty(userDO.getMobile())) {
			return JSON.toJSONString(bizResult.setMessage("手机号码为空!"));
		}

		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,1,5-9])|(17[67,8]))\\d{8}$");
		Matcher m = p.matcher(userDO.getMobile());
		if (!m.matches()) {
			return JSON.toJSONString(bizResult.setMessage("手机号码不对!"));
		}

		if (StringUtils.isEmpty(userDO.getPassword())) {
			return JSON.toJSONString(bizResult.setMessage("密码为空!"));
		}

		BizResult<UserDO> listBizResult = userService.findUserByMobile(userDO.getMobile());
		if (listBizResult.getSuccess() && listBizResult.getData() != null) {
			return JSON.toJSONString(bizResult.setMessage("手机号码已被使用!"));
		}

		bizResult = userService.register(userDO);
		return JSON.toJSONString(bizResult);
	}

	@RequestMapping(value = "/user/{userId}", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String findUserById(@PathVariable Long userId) {
		BizResult<UserDO> result = userService.findUserById(userId);
		return JSON.toJSONString(result);
	}

	@RequestMapping(value = "/user/authentication/pw", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String checkPassword(@RequestParam String mobile, @RequestParam String password) {
		BizResult<UserDO> result = userService.checkPassword(mobile, password);
		return JSON.toJSONString(result);
	}

}
