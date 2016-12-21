package com.ifair.web.index.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by feiying on 16/12/20.
 */
@Controller
public class IndexController {

	@Value("${oauth2.domain}")
	private String oauth2Domain;
	@Value("${oauth2.client.id}")
	private String oauth2ClientId;

	@RequestMapping("/index")
	public ModelAndView index() {
		ModelAndView mav = new ModelAndView("views/index");
		mav.getModel().put("loginUrl", String.format("%s/oauth2/authorize?client_id=%s&response_type=%s&redirect_uri=%s", oauth2Domain, oauth2ClientId, "code", "http://www.baidu.com"));
		return mav;
	}

}
