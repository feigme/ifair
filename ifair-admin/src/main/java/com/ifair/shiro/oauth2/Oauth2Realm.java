package com.ifair.shiro.oauth2;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

/**
 * Created by feiying on 17/1/23.
 */
public class Oauth2Realm extends AuthorizingRealm {

	private String clientId;
	private String clientSecret;
	private String accessTokenUrl;
	private String userInfoUrl;
	private String redirectUrl;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getAccessTokenUrl() {
		return accessTokenUrl;
	}

	public void setAccessTokenUrl(String accessTokenUrl) {
		this.accessTokenUrl = accessTokenUrl;
	}

	public String getUserInfoUrl() {
		return userInfoUrl;
	}

	public void setUserInfoUrl(String userInfoUrl) {
		this.userInfoUrl = userInfoUrl;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	// 验证当前Subject（可理解为当前用户）所拥有的权限，且给其授权
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
//		String currentUsername = (String)super.getAvailablePrincipal(principals);
		SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
		return authorizationInfo;
	}

	// 验证当前Subject登录
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		UsernamePasswordToken upToken = (UsernamePasswordToken)token;
		SimpleAuthenticationInfo authenticationInfo =
				new SimpleAuthenticationInfo(upToken.getUsername(), "aaa", getName());
		return authenticationInfo;
	}

	private String extractUsername(String code) {
		try {
			OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
			OAuthClientRequest accessTokenRequest = OAuthClientRequest
					.tokenLocation(accessTokenUrl)
					.setGrantType(GrantType.AUTHORIZATION_CODE)
					.setClientId(clientId)
					.setClientSecret(clientSecret)
					.setCode(code)
					.setRedirectURI(redirectUrl)
					.buildQueryMessage();
			// 获取access token
			OAuthAccessTokenResponse oAuthResponse = oAuthClient.accessToken(accessTokenRequest, OAuth.HttpMethod.POST);
			String accessToken = oAuthResponse.getAccessToken();
			Long expiresIn = oAuthResponse.getExpiresIn();
			// 获取user info
			OAuthClientRequest userInfoRequest = new OAuthBearerClientRequest(userInfoUrl).setAccessToken(accessToken).buildQueryMessage();
			OAuthResourceResponse resourceResponse = oAuthClient.resource(userInfoRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
			String username = resourceResponse.getBody();
			return username;
		} catch (Exception e) {
			throw new AuthenticationException(e);
		}
	}

}
