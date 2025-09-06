package com.tbs.shared.util;

import javax.servlet.http.HttpServletRequest;

import com.tbs.common.util.StrUtil;

public class NetUtil {
	public static String getIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (StrUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (StrUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (StrUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	public static String getMac(HttpServletRequest request) {
		String mac = request.getHeader("x-mac");
		if (StrUtil.isEmpty(mac)) {
			mac = request.getHeader("X-MAC");
		}
		if (StrUtil.isEmpty(mac)) {
			mac = request.getHeader("X-MAC");
		}
		return mac;
	}

}
