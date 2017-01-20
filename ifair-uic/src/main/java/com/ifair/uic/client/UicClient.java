package com.ifair.uic.client;

import com.alibaba.fastjson.JSON;
import com.ifair.base.BizResult;
import com.ifair.uic.domain.UserDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

/**
 * Created by feiying on 17/1/19.
 */
public class UicClient {

	private String uicDomain;

	@Autowired
	private RestTemplate restTemplate;

	public BizResult<Long> register(UserDO userDO) {
		return restTemplate.postForObject(uicDomain + "/rest/uic/register?userDO={userDO}", null, BizResult.class, JSON.toJSON(userDO));
	}

	public String getUicDomain() {
		return uicDomain;
	}

	public void setUicDomain(String uicDomain) {
		this.uicDomain = uicDomain;
	}
}
