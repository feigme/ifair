package com.ifair.user.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by feiying on 17/1/18.
 */
@Controller
public class UserController {

	@RequestMapping("/register")
	public String register(Model model) {

		return "views/register";
	}

	@RequestMapping(value = "/login")
	public String login() {
		return "views/login";
	}

}
