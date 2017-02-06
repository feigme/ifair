package com.ifair.user.web.controller;

import com.ifair.uic.client.UicClient;
import com.ifair.uic.domain.UserDO;
import com.ifair.user.command.RegisterCommand;
import org.apache.commons.lang3.StringUtils;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * Created by feiying on 16/12/20.
 */
@Controller
public class UserController {

	@Resource
	private UicClient uicClient;

	@RequestMapping("/register")
	public String register(Model model) {
		return "views/register";
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String doRegister(Model model, @Valid @ModelAttribute("uic") RegisterCommand cmd, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "views/register";
		} else {
			if (!StringUtils.equals(cmd.getPassword(), cmd.getConfirmPassword())) {
				bindingResult.rejectValue("password", "", "密码不一致!");
				return "views/register";
			}
		}

		Mapper mapper = new DozerBeanMapper();
		UserDO userDO = mapper.map(cmd, UserDO.class);

		uicClient.register(userDO);

		return "views/register";
	}

}
