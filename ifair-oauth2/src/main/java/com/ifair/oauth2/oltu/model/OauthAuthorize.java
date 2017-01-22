package com.ifair.oauth2.oltu.model;

import java.util.Date;

/**
 * 授权信息
 * 
 * Created by feiying on 16/12/23.
 */
public class OauthAuthorize {

	private Long id;
	private Long clientId;
	private Long userId;
	private String code; // 授权码
	private Date gmtCreate;
	private Date gmtModified;
	private Date endTime;

	public OauthAuthorize() {

	}

	public OauthAuthorize(Long clientId, Long userId) {
		this.clientId = clientId;
		this.userId = userId;
		gmtCreate = new Date();
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	public Date getGmtModified() {
		return gmtModified;
	}

	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
