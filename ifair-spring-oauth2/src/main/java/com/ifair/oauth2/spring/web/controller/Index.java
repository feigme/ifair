package com.ifair.oauth2.spring.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by feiying on 16/9/12.
 */
@Controller
public class Index {

    @RequestMapping("/index")
    public String index(){
        return "views/index";
    }

    @RequestMapping("/my/source")
    public String source(){
        return "views/index";
    }

    @RequestMapping("/login")
    public String login(){
        return "views/login";
    }

}
