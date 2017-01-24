package com.ifair.uic.client;

import com.alibaba.fastjson.JSON;
import com.ifair.base.BizResult;
import com.ifair.uic.domain.UserDO;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * 外部工程使用
 * 
 * Created by feiying on 17/1/19.
 */
public class UicClient {

	private String uicDomain;

	@Resource
	private RestTemplate restTemplate;

	/**
	 * 注册功能, 参数用json传递
	 * 
	 * @param userDO
	 * @return
	 */
	public BizResult<Long> register(UserDO userDO) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("application/json; charset=UTF-8"));
		HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(userDO), headers);
		String result = restTemplate.postForObject(uicDomain + "/rest/uic/register", entity, String.class);
		return JSON.parseObject(result, BizResult.class);
	}

	public BizResult<UserDO> findUserById(Long userId) {
		String result = restTemplate.getForObject(uicDomain + "/rest/uic/user/{userId}", String.class, userId);
		return JSON.parseObject(result, BizResult.class);
	}

	public BizResult<UserDO> checkPassword(String mobile, String password) {
		MultiValueMap<String, Object> urlVariables = new LinkedMultiValueMap<String, Object>();
		urlVariables.add("mobile", mobile);
		urlVariables.add("password", password);
		String result = restTemplate.getForObject(uicDomain + "/rest/uic/user/authentication/pw", null, String.class, urlVariables);
		return JSON.parseObject(result, BizResult.class);
	}

	public String getUicDomain() {
		return uicDomain;
	}

	public void setUicDomain(String uicDomain) {
		this.uicDomain = uicDomain;
	}
}
