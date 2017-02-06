package com.ifair.oauth2.oltu.domain;

import com.ifair.base.BaseDO;
import java.util.Date;

public class OauthAuthorizeDO extends BaseDO {
    private Long id;

    private Date gmtCreate;

    private Date gmtModified;

    private Long oauthClientId;

    private Long userId;

    private String code;

    private Date endTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getOauthClientId() {
        return oauthClientId;
    }

    public void setOauthClientId(Long oauthClientId) {
        this.oauthClientId = oauthClientId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}