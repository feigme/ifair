package com.ifair.common.security;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUsage {

	private static Pattern userAgentPattern = Pattern.compile("(^\\S+)/huigujia/(.*?)/");

	private static Pattern userAgentPattern2 = Pattern.compile("^[A-Za-z]+/huigujia/(.*?)/");

	public static Matcher matchUserAgent(HttpServletRequest req) {
		return matchUserAgent(req, userAgentPattern);
	}

	public static Matcher matchUserAgent2(HttpServletRequest req) {
		return matchUserAgent(req, userAgentPattern2);
	}

	private static Matcher matchUserAgent(HttpServletRequest req, Pattern pattern) {
		String userAgent = req.getHeader("User-Agent");
		if (StringUtils.isBlank(userAgent)) {
			userAgent = req.getHeader("uic-agent");
			if (StringUtils.isNotBlank(userAgent)) {
				userAgent = userAgent.toLowerCase();
			}
		} else {
			userAgent = userAgent.toLowerCase();
		}
		if (StringUtils.isNotBlank(userAgent)) {
			return pattern.matcher(userAgent);
		}
		return null;
	}

	public static String stringMask(String str) {
		if (StringUtils.isBlank(str)) {
			return "";
		}
		if (str.length() > 7) {
			return str.substring(0, 3) + "****" + str.substring(7);
		} else {
			return str.substring(0, 3) + "****";
		}
	}

	public static String generateSlat(int length) {
		char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
		int arrayLength = chars.length - 1;
		StringBuffer slat = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int position = (int) (Math.random() * arrayLength);
			slat.append(chars[position]);
		}
		return slat.toString();
	}

}
