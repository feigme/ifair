package com.ifair.shiro.tools;

import org.apache.shiro.SecurityUtils;

/**
 * Created by feiying on 17/1/25.
 */
public class ShiroTool {

	public static final String getUserName() {
		return SecurityUtils.getSubject().getPrincipal().toString();
	}

}
