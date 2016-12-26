package com.ifair.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by feiying on 16/12/20.
 */
@Controller
public class IndexController {

	@RequestMapping("/index")
	public ModelAndView index(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("views/index");

		mav.getModel().put("userName", request.getSession().getAttribute("userName"));
		return mav;
	}

}
