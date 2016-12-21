package com.ifair.oauth2.oltu.web.controller;

import com.google.common.collect.Maps;
import com.ifair.oauth2.oltu.model.OauthClient;
import com.ifair.oauth2.oltu.service.OauthClientService;
import org.apache.commons.lang.StringUtils;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * 授权控制器
 * 
 * Created by feiying on 16/9/13.
 */
@Controller
@RequestMapping("/oauth2")
public class AuthzController {

	public static final Logger log = LoggerFactory.getLogger(AuthzController.class);

	private Map<String, Object> cache = Maps.newHashMap();

	/*
	 * * 构建OAuth2授权请求 [需要client_id与redirect_uri绝对地址]
	 * 
	 * @param request
	 * 
	 * @param session
	 * 
	 * @param model
	 * 
	 * @return 返回授权码(code)有效期10分钟，客户端只能使用一次[与client_id和redirect_uri一一对应关系]
	 * 
	 * @throws OAuthSystemException
	 * 
	 * @throws IOException
	 * 
	 * @url http://localhost:8080/oauth2/authorize?client_id={AppKey}&response_type=code&redirect_uri={YourSiteUrl}
	 * 
	 * @test http://localhost:8080/oauth2/authorize?client_id=fbed1d1b4b1449daa4bc49397cbe2350&response_type=code&redirect_uri=http://localhost:8080/client/oauth_callback
	 */
	@RequestMapping(value = "/authorize")
	public String authorize(HttpServletRequest request, HttpSession session, Model model) throws OAuthSystemException, IOException {
		try {
			// 构建OAuth请求
			OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(request);

			// 验证redirecturl格式是否合法
			if (!validateRedirectionURI(oauthRequest)) {
				// @formatter:off
				OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED).setError(OAuthError.CodeResponse.INVALID_REQUEST).setErrorDescription(OAuthError.OAUTH_ERROR_URI).buildJSONMessage();
				// @formatter:on
				log.error("oauthRequest.getRedirectURI() : " + oauthRequest.getRedirectURI() + " oauthResponse.getBody() : " + oauthResponse.getBody());
				model.addAttribute("errorMsg", oauthResponse.getBody());
				return "views/oauth2/error";
			}

			// 验证appkey是否正确
			if (!validateOAuth2AppKey(oauthRequest)) {
				OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED).setError(OAuthError.CodeResponse.ACCESS_DENIED).setErrorDescription(OAuthError.CodeResponse.UNAUTHORIZED_CLIENT).buildJSONMessage();
				log.error("oauthRequest.getRedirectURI() : " + oauthRequest.getRedirectURI() + " oauthResponse.getBody() : " + oauthResponse.getBody());
				model.addAttribute("errorMsg", oauthResponse.getBody());
				return "views/oauth2/error";
			}
			// 查询客户端Appkey应用的信息
			OauthClient oauthClient =  new OauthClientService().findByClientId(oauthRequest.getClientId());
			model.addAttribute("clientName", oauthClient.getClientName());
			model.addAttribute("response_type", oauthRequest.getResponseType());
			model.addAttribute("client_id", oauthRequest.getClientId());
			model.addAttribute("redirect_uri", oauthRequest.getRedirectURI());
			model.addAttribute("scope", oauthRequest.getScopes());

			// 用户登录
			if (!validateOAuth2Pwd(request)) {
				// 登录失败跳转到登陆页
				return "views/oauth2/login";
			}

			// 判断此次请求是否是用户授权
			if (request.getParameter("action") == null || !request.getParameter("action").equalsIgnoreCase("authorize")) {
				// 到申请用户同意授权页
				// TODO 判断用户是否已经授权
				return "views/oauth2/authorize";
			}
			// 生成授权码 UUIDValueGenerator OR MD5Generator
			String authorizationCode = new OAuthIssuerImpl(new MD5Generator()).authorizationCode();
			// 把授权码存入缓存
			//cache.put(authorizationCode, DigestUtils.sha1Hex(oauthRequest.getClientId() + oauthRequest.getRedirectURI()));
			// 构建oauth2授权返回信息
			OAuthResponse oauthResponse = OAuthASResponse.authorizationResponse(request, HttpServletResponse.SC_FOUND).setCode(authorizationCode).location(oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI)).buildQueryMessage();
			// 申请令牌成功重定向到客户端页
			return "redirect:" + oauthResponse.getLocationUri();
		} catch (OAuthProblemException ex) {
			OAuthResponse oauthResponse = OAuthResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED).error(ex).buildJSONMessage();
			log.error("oauthRequest.getRedirectURI() : " + ex.getRedirectUri() + " oauthResponse.getBody() : " + oauthResponse.getBody());
			model.addAttribute("errorMsg", oauthResponse.getBody());
			return "views/oauth2/error";
		}
	}

	private boolean validateRedirectionURI(OAuthAuthzRequest oauthRequest) {
		// TODO
		return true;
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login() {
		return "views/oauth2/login";
	}

	/**
	 * 用户登录
	 * 
	 * @param request
	 * @return
	 */
	private boolean validateOAuth2Pwd(HttpServletRequest request) {
		if ("get".equalsIgnoreCase(request.getMethod())) {
			return false;
		}

		String name = request.getParameter("username");
		String pwd = request.getParameter("password");
		if (StringUtils.isEmpty(name) || StringUtils.isEmpty(pwd)) {
			return false;
		}

		// 已登录
		if (cache.get(name)!=null){
			return true;
		}

		try {
			if (name.equalsIgnoreCase("test") && pwd.equalsIgnoreCase("123456")) {
				// 登录成功
				cache.put(name, true);
				return true;
			}
			return false;
		} catch (Exception ex) {
			log.error("validateOAuth2Pwd Exception: " + ex.getMessage());
			return false;
		}
	}

	/**
	 * 验证ClientID 是否正确
	 * 
	 * @param oauthRequest
	 * @return
	 */
	public boolean validateOAuth2AppKey(OAuthAuthzRequest oauthRequest) {
		return new OauthClientService().findByClientId(oauthRequest.getClientId()) != null;
	}

}
