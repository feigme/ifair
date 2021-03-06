package com.ifair.oauth2.oltu.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.ifair.base.BizResult;
import com.ifair.oauth2.oltu.service.OauthClientService;
import com.ifair.uic.client.UicClient;
import com.ifair.uic.domain.UserDO;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by feiying on 16/9/22.
 */
@RestController
@RequestMapping("/oauth2")
public class ResourceController {

	private static Logger logger = LoggerFactory.getLogger(ResourceController.class);

	@Resource
	private OauthClientService oauthClientService;

	@Resource
	private UicClient uicClient;

	@RequestMapping(value = "/get_resource", produces = "application/json;charset=utf-8")
	@ResponseBody
	public String get_resource(HttpServletRequest request, HttpServletResponse response) throws IOException, OAuthSystemException {
		try {
			// 构建oauth2资源请求
			OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(request, ParameterStyle.QUERY);
			// 获取验证accesstoken
			String accessToken = oauthRequest.getAccessToken();
			// 验证accesstoken是否存在或过期
			if (accessToken == null) {
				OAuthResponse oauthResponse = OAuthRSResponse
						.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
						.setRealm("RESOURCE_SERVER_NAME")
						.setError(OAuthError.ResourceResponse.INVALID_TOKEN)
						.setErrorDescription(OAuthError.ResourceResponse.EXPIRED_TOKEN)
						.buildHeaderMessage();
				response.addDateHeader(OAuth.HeaderType.WWW_AUTHENTICATE, Long.parseLong(oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE)));
				return "";
			}

			Long userId = (Long) oauthClientService.get(accessToken);
			BizResult<UserDO> userDOBizResult = uicClient.findUserById(userId);
			if (!userDOBizResult.getSuccess()){
				OAuthResponse oauthResponse = OAuthRSResponse
						.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
						.setRealm("user not found")
						.buildHeaderMessage();
				response.addDateHeader(OAuth.HeaderType.WWW_AUTHENTICATE, Long.parseLong(oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE)));
				return "";
			}
			UserDO oauthUser = userDOBizResult.getData();

			// json屏蔽一些字段
			SimplePropertyPreFilter simplePropertyPreFilter = new SimplePropertyPreFilter();
			simplePropertyPreFilter.getExcludes().add("password");
			simplePropertyPreFilter.getExcludes().add("salt");
			return JSON.toJSONString(oauthUser, simplePropertyPreFilter);
		} catch (OAuthProblemException ex) {
			logger.error("ResourceController OAuthProblemException : " + ex.getMessage());
			OAuthResponse oauthResponse = OAuthRSResponse
					.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
					.setRealm("get_resource exception")
					.buildHeaderMessage();
			response.addDateHeader(OAuth.HeaderType.WWW_AUTHENTICATE, Long.parseLong(oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE)));
		}
		return "";
	}

}
