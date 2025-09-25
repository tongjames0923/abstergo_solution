package com.tbs.web.security.impl;

import com.alibaba.fastjson2.JSON;
import com.tbs.web.enums.TokenSource;
import com.tbs.web.exception.NotFoundTokenException;
import com.tbs.web.gate.ITokenPicker;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public class SimpleTokenPicker implements ITokenPicker {

    protected String fieldName = "";
    protected TokenSource tokenSource = TokenSource.HEADER;
    protected int expireTime = 0;

    private String getBody(HttpServletRequest request) {
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
                Map map = JSON.parseObject(body, Map.class);
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