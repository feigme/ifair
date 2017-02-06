package com.ifair.oauth2.oltu.web.controller;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by feiying on 16/9/23.
 */
@Controller
@RequestMapping("/client")
public class ClientController {

	public static final Logger log = LoggerFactory.getLogger(ClientController.class);

	/*
	 * response_type：表示授权类型，必选项，此处的值固定为"code" client_id：表示客户端的ID，必选项 redirect_uri：表示重定向URI，可选项 scope：表示申请的权限范围，可选项 state：表示客户端的当前状态，可以指定任意值，认证服务器会原封不动地返回这个值
	 * 
	 * 获得授权码
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String client() {
		try {
			OAuthClientRequest oauthResponse = OAuthClientRequest
					.authorizationLocation("http://127.0.0.1:8080/oauth2/authorize")
					.setResponseType(OAuth.OAUTH_CODE)
					.setClientId("client_1")
					.setRedirectURI("http://127.0.0.1:8080/client/oauth_callback")
					.setScope("read")
					.buildQueryMessage();
			return "redirect:" + oauthResponse.getLocationUri();
		} catch (OAuthSystemException e) {
			e.printStackTrace();
		}
		return "redirect:/home";
	}

	/*
	 * grant_type：表示使用的授权模式，必选项，此处的值固定为"authorization_code"
	 *
	 * code：表示上一步获得的授权码，必选项。
	 *
	 * redirect_uri：表示重定向URI，必选项，且必须与A步骤中的该参数值保持一致
	 *
	 * client_id：表示客户端ID，必选项
	 * 
	 * 获得令牌
	 * 
	 * @return oauth_callback?code=1234
	 */
	@RequestMapping(value = "/oauth_callback", method = RequestMethod.GET)
	@ResponseBody
	public String getToken(HttpServletRequest request, Model model) throws OAuthProblemException {
		OAuthAuthzResponse oauthAuthzResponse = null;
		try {
			oauthAuthzResponse = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
			String code = oauthAuthzResponse.getCode();
			OAuthClientRequest oauthClientRequest = OAuthClientRequest
					.tokenLocation("http://localhost:8080/oauth2/access_token")
					.setGrantType(GrantType.AUTHORIZATION_CODE)
					.setClientId("client_1")
					.setClientSecret("aaabbb")
					.setRedirectURI("http://localhost:8080/client/oauth_callback")
					.setCode(code)
					.buildQueryMessage();
			OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

			// Facebook is not fully compatible with OAuth 2.0 draft 10, access token response is
			// application/x-www-form-urlencoded, not json encoded so we use dedicated response class for that
			// Custom response classes are an easy way to deal with oauth providers that introduce modifications to
			// OAuth 2.0 specification

			// 获取access token
			OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(oauthClientRequest, OAuth.HttpMethod.POST);
			String accessToken = oAuthResponse.getAccessToken();
			String refreshToken = oAuthResponse.getRefreshToken();
			Long expiresIn = oAuthResponse.getExpiresIn();
			// 获得资源服务
			OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest("http://localhost:8080/oauth2/get_resource").setAccessToken(accessToken).buildQueryMessage();
			OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
			String resBody = resourceResponse.getBody();
			log.info("accessToken: " + accessToken + " refreshToken: " + refreshToken + " expiresIn: " + expiresIn + " resBody: " + resBody);
			model.addAttribute("accessToken", "accessToken: " + accessToken + " resBody: " + resBody);
			return resBody;
		} catch (OAuthSystemException ex) {
			log.error("getToken OAuthSystemException : " + ex.getMessage());
			model.addAttribute("errorMsg", ex.getMessage());
			return "/oauth2/error";
		}
	}

}
