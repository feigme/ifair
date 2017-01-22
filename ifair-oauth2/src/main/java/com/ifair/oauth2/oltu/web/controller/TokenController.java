package com.ifair.oauth2.oltu.web.controller;

import com.alibaba.fastjson.JSON;
import com.ifair.oauth2.oltu.service.OauthClientService;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by feiying on 16/9/14.
 */
@RestController
@RequestMapping("/oauth2")
public class TokenController {

	public static final Logger log = LoggerFactory.getLogger(TokenController.class);

	private OauthClientService oauthClientService = new OauthClientService();

	/**
	 * 认证服务器申请令牌(AccessToken) [验证client_id、client_secret、auth code的正确性或更新令牌 refresh_token]
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @url http://localhost:8080/oauth2/access_token?client_id={AppKey}&client_secret={AppSecret}&grant_type=authorization_code&redirect_uri={YourSiteUrl}&code={code}
	 */
	@RequestMapping(value = "/access_token", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String access_token(HttpServletRequest request, HttpServletResponse response) throws IOException, OAuthSystemException {
		OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
		try {
			// 构建oauth2请求
			OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);
			// 验证redirecturl格式是否合法
			if (!validateRedirectionURI(oauthRequest)) {
				OAuthResponse oauthResponse = OAuthASResponse
						.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
						.setError(OAuthError.CodeResponse.INVALID_REQUEST)
						.setErrorDescription(OAuthError.OAUTH_ERROR_URI)
						.buildJSONMessage();
				return JSON.toJSONString(oauthResponse.getBody());
			}
			// 验证appkey是否正确
			if (!validateOAuth2AppKey(oauthRequest)) {
				OAuthResponse oauthResponse = OAuthASResponse
						.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
						.setError(OAuthError.CodeResponse.ACCESS_DENIED)
						.setErrorDescription(OAuthError.CodeResponse.UNAUTHORIZED_CLIENT)
						.buildJSONMessage();
				return JSON.toJSONString(oauthResponse.getBody());
			}
			// 验证客户端安全AppSecret是否正确
			if (!validateOAuth2AppSecret(oauthRequest)) {
				OAuthResponse oauthResponse = OAuthASResponse
						.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
						.setError(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT)
						.setErrorDescription("INVALID_CLIENT_SECRET")
						.buildJSONMessage();
				return JSON.toJSONString(oauthResponse.getBody());
			}

			String authzCode = oauthRequest.getCode();

			// 验证AUTHORIZATION_CODE, 其他的还有PASSWORD 或 REFRESH_TOKEN (考虑到更新令牌的问题，在做修改)
			if (GrantType.AUTHORIZATION_CODE.name().equalsIgnoreCase(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE))) {
				// 是否需要验证 code是否属于clientId
				if (oauthClientService.get(authzCode)==null || !oauthClientService.get(oauthRequest.getClientId()).equals(authzCode)) {
					OAuthResponse oauthResponse = OAuthASResponse
							.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
							.setError(OAuthError.TokenResponse.INVALID_GRANT)
							.setErrorDescription("INVALID_CLIENT_GRANT")
							.buildJSONMessage();
					return JSON.toJSONString(oauthResponse.getBody());
				}
			}
			// 生成token
			final String accessToken = oauthIssuerImpl.accessToken();
			String refreshToken = oauthIssuerImpl.refreshToken();
			log.info("accessToken : " + accessToken + "  refreshToken: " + refreshToken);
			oauthClientService.put(accessToken, oauthClientService.get(authzCode));

			// 清除授权码 确保一个code只能使用一次
			oauthClientService.evict(authzCode);

			// 构建oauth2授权返回信息
			OAuthResponse oauthResponse = OAuthASResponse
					.tokenResponse(HttpServletResponse.SC_OK)
					.setAccessToken(accessToken)
					.setExpiresIn("3600")
					.setRefreshToken(refreshToken)
					.buildJSONMessage();
			response.setStatus(oauthResponse.getResponseStatus());
			return oauthResponse.getBody();
		} catch (OAuthProblemException ex) {
			OAuthResponse oauthResponse = OAuthResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED).error(ex).buildJSONMessage();
			response.setStatus(oauthResponse.getResponseStatus());
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return JSON.toJSONString(oauthResponse.getBody());
		}
	}

	/**
	 * 刷新令牌
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws OAuthSystemException
	 * @url http://localhost:8080/oauth2/refresh_token?client_id={AppKey}&grant_type=refresh_token&refresh_token={refresh_token}
	 */
	@RequestMapping(value = "/refresh_token", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
	@ResponseBody
	public String refresh_token(HttpServletRequest request, HttpServletResponse response) throws IOException, OAuthSystemException {
		PrintWriter out = null;
		OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
		try {
			// 构建oauth2请求
			OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);
			// 验证appkey是否正确
			if (!validateOAuth2AppKey(oauthRequest)) {
				OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED).setError(OAuthError.CodeResponse.ACCESS_DENIED).setErrorDescription(OAuthError.CodeResponse.UNAUTHORIZED_CLIENT).buildJSONMessage();
				return JSON.toJSONString(oauthResponse.getBody());
			}
			// 验证是否是refresh_token
			if (GrantType.REFRESH_TOKEN.name().equalsIgnoreCase(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE))) {
				OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED).setError(OAuthError.TokenResponse.INVALID_GRANT).setErrorDescription("INVALID_CLIENT_GRANT").buildJSONMessage();
				return JSON.toJSONString(oauthResponse.getBody());
			}
			/*
			 * 刷新access_token有效期 access_token是调用授权关系接口的调用凭证，由于access_token有效期（目前为2个小时）较短，当access_token超时后，可以使用refresh_token进行刷新，access_token刷新结果有两种： 1.
			 * 若access_token已超时，那么进行refresh_token会获取一个新的access_token，新的超时时间； 2. 若access_token未超时，那么进行refresh_token不会改变access_token，但超时时间会刷新，相当于续期access_token。
			 * refresh_token拥有较长的有效期（30天），当refresh_token失效的后，需要用户重新授权。
			 */
			// Object cache_refreshToken=GuavaCache.cache.getIfPresent(oauthRequest.getRefreshToken());
			// access_token已超时
			// if (cache_refreshToken == null) {
			// //生成token
			// final String access_Token = oauthIssuerImpl.accessToken();
			// String refresh_Token = oauthIssuerImpl.refreshToken();
			// //cache.put(refresh_Token,access_Token);
			// log.info("access_Token : "+access_Token +" refresh_Token: "+refresh_Token);
			// //构建oauth2授权返回信息
			// OAuthResponse oauthResponse = OAuthASResponse
			// .tokenResponse(HttpServletResponse.SC_OK)
			// .setAccessToken(access_Token)
			// .setExpiresIn("3600")
			// .setRefreshToken(refresh_Token)
			// .buildJSONMessage();
			// response.setStatus(oauthResponse.getResponseStatus());
			// out.print(oauthResponse.getBody());
			// out.flush();
			// out.close();
			// return;
			// }
			// access_token未超时
			// GuavaCache.cache.put(oauthRequest.getRefreshToken(),cache_refreshToken.toString());
			// 构建oauth2授权返回信息
			OAuthResponse oauthResponse = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK).setAccessToken("cache_refresh").setExpiresIn("3600").setRefreshToken(oauthRequest.getRefreshToken()).buildJSONMessage();
			response.setStatus(oauthResponse.getResponseStatus());
			return JSON.toJSONString(oauthResponse.getBody());
		} catch (OAuthProblemException ex) {
			OAuthResponse oauthResponse = OAuthResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED).error(ex).buildJSONMessage();
			response.setStatus(oauthResponse.getResponseStatus());
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return JSON.toJSONString(oauthResponse.getBody());
		}
	}

	/**
	 * 验证ClientID 是否正确
	 * 
	 * @param oauthRequest
	 * @return
	 */
	public boolean validateOAuth2AppKey(OAuthTokenRequest oauthRequest) {
		return oauthClientService.findClientByClientId(oauthRequest.getClientId()) != null;
	}

	/**
	 * 验证AppSecret 是否正确
	 * 
	 * @param oauthRequest
	 * @return
	 */
	public boolean validateOAuth2AppSecret(OAuthTokenRequest oauthRequest) {
		return oauthClientService.findClientByClientId(oauthRequest.getClientId()).getClientSecret().equals(oauthRequest.getClientSecret());
	}

	private boolean validateRedirectionURI(OAuthTokenRequest tokenRequest) {
		// TODO
		return true;
	}
}
