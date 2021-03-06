package com.ifair.oauth2.oltu.web.controller;

import com.ifair.base.BizResult;
import com.ifair.common.security.DesCbcSecurity;
import com.ifair.oauth2.oltu.domain.OauthAuthorizeDO;
import com.ifair.oauth2.oltu.domain.OauthClientDO;
import com.ifair.oauth2.oltu.service.OauthClientService;
import com.ifair.oauth2.oltu.utils.OauthUtils;
import com.ifair.uic.client.UicClient;
import com.ifair.uic.domain.UserDO;
import org.apache.commons.lang.StringUtils;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 授权控制器
 * 
 * Created by feiying on 16/9/13.
 */
@Controller
@RequestMapping("/oauth2")
public class AuthzController {

	public static final Logger log = LoggerFactory.getLogger(AuthzController.class);

	@Resource
	private OauthClientService oauthClientService;

	@Resource
	private UicClient uicClient;

	public static final String COOKIE_SESSION_KEY = "sid";

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
	public String authorize(HttpServletRequest request, HttpServletResponse response, HttpSession session, Model model) throws Exception {
		try {
			// 构建OAuth请求
			OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(request);

			// 验证redirecturl格式是否合法
			if (!validateRedirectionURI(oauthRequest)) {
				// @formatter:off
				OAuthResponse oauthResponse = OAuthASResponse
						.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
						.setError(OAuthError.CodeResponse.INVALID_REQUEST)
						.setErrorDescription(OAuthError.OAUTH_ERROR_URI)
						.buildJSONMessage();
				// @formatter:on
				log.error("oauthRequest.getRedirectURI() : " + oauthRequest.getRedirectURI() + " oauthResponse.getBody() : " + oauthResponse.getBody());
				model.addAttribute("errorMsg", oauthResponse.getBody());
				return "views/oauth2/error";
			}

			// 查询客户端Appkey应用的信息
			OauthClientDO oauthClient =  oauthClientService.findClientByClientId(oauthRequest.getClientId());

			// 验证appkey是否正确
			if (!validateOAuth2AppKey(oauthClient)) {
				OAuthResponse oauthResponse = OAuthASResponse
						.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
						.setError(OAuthError.CodeResponse.ACCESS_DENIED)
						.setErrorDescription(OAuthError.CodeResponse.UNAUTHORIZED_CLIENT)
						.buildJSONMessage();
				log.error("oauthRequest.getRedirectURI() : " + oauthRequest.getRedirectURI() + " oauthResponse.getBody() : " + oauthResponse.getBody());
				model.addAttribute("errorMsg", oauthResponse.getBody());
				return "views/oauth2/error";
			}

			model.addAttribute("clientName", oauthClient.getClientName());
			model.addAttribute("response_type", oauthRequest.getResponseType());
			model.addAttribute("client_id", oauthRequest.getClientId());
			model.addAttribute("redirect_uri", oauthRequest.getRedirectURI());
			model.addAttribute("scope", oauthRequest.getScopes());

			// Session中的用户信息
			UserDO oauthUser = (UserDO) session.getAttribute("USER_SESSION_KEY");
			if (oauthUser == null) {
				// 缓存中的用户信息
				oauthUser = (UserDO) getCacheBySessionId(request, "oauth_");
			}

			// 判断用户是否已登录
			if (oauthUser == null) {
				// 用户登录
				if (!validateOAuth2Pwd(request, response)) {
					// 登录失败跳转到登陆页
					return "views/login";
				}
				oauthUser = (UserDO) session.getAttribute("USER_SESSION_KEY");
			}

			// 判断用户是否已经授权
			OauthAuthorizeDO oauthAuthorize = oauthClientService.findAuthorize(oauthClient, oauthUser);
			if (oauthAuthorize == null) {
				// 判断此次请求是否是用户授权
				if (request.getParameter("action") != null && request.getParameter("action").equalsIgnoreCase("authorize")) {
					// 保存授权信心
					oauthClientService.authorize(oauthClient, oauthUser);
				}else{
					// 到申请用户同意授权页
					return "views/oauth2/authorize";
				}
			}

			// 生成授权码 UUIDValueGenerator OR MD5Generator
			String authorizationCode = new OAuthIssuerImpl(new MD5Generator()).authorizationCode();
			// 更新授权码
			oauthClientService.put(oauthRequest.getClientId(), authorizationCode);
			oauthClientService.put(authorizationCode, oauthUser.getId());
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

	/**
	 * 用户登录
	 *
	 * @param request
	 * @return
	 */
	private boolean validateOAuth2Pwd(HttpServletRequest request, HttpServletResponse response) {
		if ("get".equalsIgnoreCase(request.getMethod())) {
			return false;
		}

		String name = request.getParameter("username");
		String pwd = request.getParameter("password");
		if (StringUtils.isEmpty(name) || StringUtils.isEmpty(pwd)) {
			return false;
		}

		try {
			 BizResult<UserDO> checkBizResult = uicClient.checkPassword(name, pwd);
			if (checkBizResult.getSuccess() && checkBizResult.getData() != null) {
				UserDO oauthUser = checkBizResult.getData();
				// 登录成功
				request.getSession().setAttribute("USER_SESSION_KEY", oauthUser);

				// 写cookie
				String sessionId = OauthUtils.generateSessionId(request);
				setCookies(COOKIE_SESSION_KEY, sessionId, true, 14, request, response);
				String md5String = DesCbcSecurity.md5("oauth_" + sessionId);
				oauthClientService.put(md5String, oauthUser);

				return true;
			}
			return false;
		} catch (Exception ex) {
			log.error("validateOAuth2Pwd Exception: " + ex.getMessage(), ex);
			return false;
		}
	}

	/**
	 * 验证ClientID 是否正确
	 *
	 * @param oauthClient
	 * @return
	 */
	private boolean validateOAuth2AppKey(OauthClientDO oauthClient) {
		return oauthClient != null;
	}

	private Object getCacheBySessionId(HttpServletRequest req, String key) {
		Cookie[] cookies = req.getCookies();
		if (cookies == null) {
			log.error("[] cookies is null");
			return null;
		}
		String sessionId = null;
		for (int i = 0; i < cookies.length; i++) {
			if (cookies[i].getName().equals(COOKIE_SESSION_KEY)) {
				sessionId = cookies[i].getValue();
				break;
			}
		}
		if (sessionId == null) {
			return null;
		} else {
			try {
				String md5String = null;
				md5String = DesCbcSecurity.md5(key + sessionId);
				return oauthClientService.get(md5String);
			} catch (Exception e) {
				return null;
			}
		}
	}

	/**
	 * 设置cookie
	 *
	 * @param key
	 * @param value
	 * @param maxAge
	 * @param response
	 * @param req
	 * @param supportSubDomainSharing
	 *            是否在所有的子域名中共享
	 */
	private void setCookies(String key, String value, int maxAge, HttpServletResponse response, HttpServletRequest req, boolean supportSubDomainSharing) {
		// 写cookie
		Cookie cookie = new Cookie(key, value);

		if (supportSubDomainSharing) {
			String domainName = req.getServerName();
			if (domainName.indexOf("localhost") < 0 && domainName.indexOf("ywy") < 0) {
				String domainNamePrefix = domainName.substring(domainName.indexOf("."), domainName.length());
				// Specifies the domain within which this cookie should be presented.
				cookie.setDomain(domainNamePrefix);
			}
		}
		cookie.setPath("/");
		// 生命周期
		cookie.setMaxAge(maxAge);
		response.addCookie(cookie);
	}

	private void setCookies(String key, String value, boolean rememberLogin, int rememberDays, HttpServletRequest req, HttpServletResponse resp) {
		setCookies(key, value, 0, resp);
		setCookies(key, value, rememberLogin ? 3600 * 24 * rememberDays : 3600 * 24, resp, req, true);
	}

	private void setCookies(String key, String value, int maxAge, HttpServletResponse response) {
		setCookies(key, value, maxAge, response, null, false);
	}

}
