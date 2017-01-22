package com.ifair.oauth2.oltu.model;

/**
 * 申请的客户信息
 * 
 * Created by feiying on 16/9/23.
 */
public class OauthClient {

	private Long id;
	private String clientId;
	private String clientSecret;
	private String clientName;
	private String responseType;
	private String redirectUri;
	private String scope;

	public OauthClient() {
	}

	public OauthClient(Long id, String clientId, String clientSecret, String clientName, String responseType, String redirectUri, String scope) {
		this.id = id;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.clientName = clientName;
		this.responseType = responseType;
		this.redirectUri = redirectUri;
		this.scope = scope;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getResponseType() {
		return responseType;
	}

	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
}
