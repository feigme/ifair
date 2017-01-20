package com.ifair.oauth2.oltu.utils;

import com.ifair.common.security.RandomString;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

/**
 * Created by feiying on 16/12/23.
 */
public class OauthUtils {

	/**
	 *
	 * @param request
	 * @return
	 */
	public static String generateSessionId(HttpServletRequest request) {
		// uniqid
		String uniqIdString = uniqid("", true);
		// ip地址
		String ipString = remoteAddress(request);
		// 随机25位字符串
		String randomString = new RandomString(25).nextString();
		// 当前时间
		long curtime = System.currentTimeMillis();

		return DigestUtils.sha1Hex(uniqIdString + ipString + randomString + curtime);
	}

	/**
	 *
	 * @param prefix
	 * @param more_entropy
	 * @return
	 */
	public static String uniqid(String prefix, boolean more_entropy) {
		long time = System.currentTimeMillis();
		String uniqid;
		if (!more_entropy) {
			uniqid = String.format("%s%08x%05x", prefix, time / 1000, time);
		} else {
			SecureRandom sec = new SecureRandom();
			byte[] sbuf = sec.generateSeed(8);
			ByteBuffer bb = ByteBuffer.wrap(sbuf);

			uniqid = String.format("%s%08x%05x", prefix, time / 1000, time);
			uniqid += "." + String.format("%.8s", "" + bb.getLong() * -1);
		}
		return uniqid;
	}

	/**
	 * 获取请求的ip地址
	 *
	 * @param request
	 * @return
	 */
	public static String remoteAddress(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (StringUtils.isBlank(ip)) {
			ip = request.getHeader("x-forwarded-for");
		}
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
			ip = request.getHeader("X-Real-IP");
		}
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) {
			ip = request.getRemoteAddr();
		}
		// ip超长处理
		if (ip != null && ip.contains(",")) {
			int index = ip.indexOf(",");
			if (index > 16) {
				index = 16;
			}
			ip = ip.substring(0, index);
		}

		// ipv6, localhost的地址处理
		String localhostIpv6 = "0:0:0:0:0:0:0:1";
		if (localhostIpv6.equals(ip)) {
			ip = "127.0.0.1";
		}

		return ip;
	}

}
