package com.tbs.shared.util;

import com.tbs.common.util.StrUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * 网络工具类，提供获取客户端IP和MAC地址等网络相关功能 继承自hutool的NetUtil类，扩展了Web环境下的网络信息获取功能
 */
public class NetUtil extends cn.hutool.core.net.NetUtil {
    /**
     * 从HTTP请求中获取客户端IP地址
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getIp(HttpServletRequest request) {
        // 尝试从不同的请求头中获取IP地址
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

    /**
     * 从HTTP请求中获取客户端MAC地址
     *
     * @param request HTTP请求对象
     * @return 客户端MAC地址
     */
    public static String getMac(HttpServletRequest request) {
        // 尝试从不同的请求头中获取MAC地址
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
