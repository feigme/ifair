package com.ifair.web.index.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by feiying on 16/12/20.
 */
@Controller
public class IndexController {

	@RequestMapping("/index")
	public ModelAndView index() {
		ModelAndView mav = new ModelAndView("views/index");
		return mav;
	}

}
