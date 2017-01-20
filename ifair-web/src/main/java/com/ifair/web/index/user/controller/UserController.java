package com.ifair.web.index.user.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ifair.uic.client.UicClient;
import com.ifair.uic.domain.UserDO;
import com.ifair.web.index.user.command.RegisterCommand;
import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * Created by feiying on 16/12/20.
 */
@Controller
public class UserController {

    public static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Value("${oauth2.domain}")
    private String oauth2Domain;
    @Value("${oauth2.client.id}")
    private String oauth2ClientId;
    @Value("${oauth2.client.secret}")
    private String oauth2ClientSecret;

    @Value("${web.domain}")
    private String webDomain;

    @Resource
    private UicClient uicClient;


    @RequestMapping("/login")
    public String login() throws Exception{
        OAuthClientRequest oAuthClientRequest = OAuthClientRequest
                .authorizationLocation(oauth2Domain + "/oauth2/authorize")
                .setResponseType(OAuth.OAUTH_CODE)
                .setClientId(oauth2ClientId)
                .setRedirectURI(webDomain +"/oauth_callback")
                .setScope("read")
                .buildQueryMessage();
        return "redirect:" + oAuthClientRequest.getLocationUri();
    }

    @RequestMapping("/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		request.getSession().invalidate();
		Cookie[] cookies = request.getCookies();
		for (Cookie cookie : cookies) {
			setCookies(cookie.getName(), null, 0, response);
		}
		return "redirect:/index";
	}
    
	private void setCookies(String key, String value, int maxAge, HttpServletResponse response) {
		// 写cookie
		Cookie cookie = new Cookie(key, value);

		cookie.setPath("/");
		// 生命周期
		cookie.setMaxAge(maxAge);
		response.addCookie(cookie);
	}

    @RequestMapping("/oauth_callback")
    public String userInfo(HttpServletRequest request) throws Exception{
        OAuthAuthzResponse oauthAuthzResponse = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
        OAuthClientRequest oauthClientRequest = OAuthClientRequest
                .tokenLocation(oauth2Domain + "/oauth2/access_token")
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setClientId(oauth2ClientId)
                .setClientSecret(oauth2ClientSecret)
                .setRedirectURI(webDomain + "/index")
                .setCode(oauthAuthzResponse.getCode())
                .buildQueryMessage();
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        // 获取access token
        OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(oauthClientRequest, OAuth.HttpMethod.POST);
        String accessToken = oAuthResponse.getAccessToken();
        String refreshToken = oAuthResponse.getRefreshToken();
        Long expiresIn = oAuthResponse.getExpiresIn();
        // 获得资源服务
        OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(oauth2Domain+"/oauth2/get_resource").setAccessToken(accessToken).buildQueryMessage();
        OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
        String resBody = resourceResponse.getBody();
        log.info("accessToken: " + accessToken + " refreshToken: " + refreshToken + " expiresIn: " + expiresIn + " resBody: " + resBody);

        JSONObject jsonObject = JSON.parseObject(resBody);
        request.getSession().setAttribute("userName", jsonObject.get("name"));

        return "redirect:" + webDomain + "/index";
    }

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
