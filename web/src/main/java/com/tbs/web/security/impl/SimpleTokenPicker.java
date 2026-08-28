package com.tbs.web.security.impl;

import com.tbs.common.util.base.JsonUtils;
import com.tbs.web.enums.TokenSource;
import com.tbs.web.exception.NotFoundTokenException;
import com.tbs.web.gate.ITokenPicker;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

/**
 * 简单令牌拾取器
 * <p>
 * 按配置的 {@code tokenSource}（header/cookie/param/body）与 {@code fieldName} 从请求中提取令牌串。
 * 提取规则由子类（如 {@code DefaultLoginTokenPicker}）通过 {@code @PostConstruct} 注入。
 * </p>
 *
 * @see ITokenPicker 令牌拾取契约
 */
public class SimpleTokenPicker implements ITokenPicker {

    protected String fieldName = "";
    protected TokenSource tokenSource = TokenSource.HEADER;
    protected int expireTime = 0;

    private String getBody(HttpServletRequest request) {
        // 读取请求体原文，供 BODY 来源解析 JSON 中的令牌字段
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private String getFromCookie(HttpServletRequest request, String name) {
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (javax.servlet.http.Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String getFromParam(HttpServletRequest request, String name) {
        return request.getParameter(name);
    }

    @Override
    public String pick(HttpServletRequest request) throws NotFoundTokenException {
        switch (tokenSource) {
            case BODY: {
                String body = getBody(request);
                Map map = JsonUtils.parseObject(body, Map.class);
                return map.get(fieldName).toString();
            }
            case COOKIE: {
                return getFromCookie(request, fieldName);
            }
            case HEADER: {
                return request.getHeader(fieldName);
            }
            case PARAM: {
                return getFromParam(request, fieldName);
            }
            default:
                return null;
        }
    }
}