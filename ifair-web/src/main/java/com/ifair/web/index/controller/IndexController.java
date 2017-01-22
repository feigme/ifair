package com.ifair.web.index.controller;

import com.ifair.uic.client.UicClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by feiying on 16/12/20.
 */
@Controller
public class IndexController {

	@Resource
	private UicClient uicClient;

	@RequestMapping("/index")
	public ModelAndView index(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("views/index");

		mav.getModel().put("userName", request.getSession().getAttribute("userName"));
		mav.getModel().put("test", uicClient.findUserById(7L));
		return mav;
	}

}
