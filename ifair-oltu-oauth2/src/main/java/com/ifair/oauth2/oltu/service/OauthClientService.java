package com.ifair.oauth2.oltu.service;

import com.ifair.oauth2.oltu.model.OauthClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by feiying on 16/9/23.
 */
public class OauthClientService {

	private static Map<String, OauthClient> clientMap = new HashMap<String, OauthClient>();

	static {
		clientMap.put("client_1", new OauthClient(1L, "client_1", "aaabbb", "斯柯达", "code", "http://www.baidu.com", null));
		clientMap.put("client_2", new OauthClient(2L, "client_2", "bbbccc","上岛咖啡", "code", "http://www.taobao.com", null));
	}

	public OauthClient findByClientId(String clientId) {
		return clientMap.get(clientId);
	}

}
