package com.ifair.web.index.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by feiying on 16/12/20.
 */
@Controller
public class IndexController {

	@Value("${oauth2.domain}")
	protected String oauth2Domain;

	@ModelAttribute("oauth2Domain")
	public String oauth2Domain() {
		return oauth2Domain;
	}

	@RequestMapping("/index")
	public ModelAndView index() {
		return new ModelAndView("views/index");
	}

}
