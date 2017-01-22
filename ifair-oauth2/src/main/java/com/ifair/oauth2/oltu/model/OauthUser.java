package com.ifair.oauth2.oltu.model;

/**
 * 用户信息
 * 
 * Created by feiying on 16/12/23.
 */
public class OauthUser {

	private Long id;
	private String userName;
	private String password;

	public OauthUser() {

	}

	public OauthUser(Long id, String userName, String password) {
		this.id = id;
		this.userName = userName;
		this.password = password;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
