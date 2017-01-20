package com.ifair.uic.client;

import com.alibaba.fastjson.JSON;
import com.ifair.base.BizResult;
import com.ifair.uic.domain.UserDO;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * Created by feiying on 17/1/19.
 */
public class UicClient {

	private String uicDomain;

	@Resource
	private RestTemplate restTemplate;

	public BizResult<Long> register(UserDO userDO) {
		String result = restTemplate.postForObject(uicDomain + "/rest/uic/register?userDO={userDO}", null, String.class, JSON.toJSON(userDO));
		return JSON.parseObject(result, BizResult.class);
	}

	public String getUicDomain() {
		return uicDomain;
	}

	public void setUicDomain(String uicDomain) {
		this.uicDomain = uicDomain;
	}
}
