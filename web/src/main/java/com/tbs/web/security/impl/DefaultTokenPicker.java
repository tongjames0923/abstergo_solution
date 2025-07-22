package com.tbs.web.security.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import com.tbs.common.util.AssertUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.exception.NotFoundTokenException;
import com.tbs.web.security.model.SimpleTokenModel;
import com.tbs.web.security.ITokenPicker;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

/**
 * 默认的TokenPicker实现
 *
 * @author TBS
 * @date 2022/12/01 09:07
 **/
public class DefaultTokenPicker implements ITokenPicker {

    @Resource
    WebAuthProperty webAuthProperty;

    private String getBody(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try {
            try (var reader = request.getReader()) {
                reader.lines().forEach(sb::append);
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String getFromCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
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
    public com.tbs.web.security.ITokenModel pick(HttpServletRequest request) throws NotFoundTokenException {
        String token = "", sourceField, sourceValue = "";
        sourceField = webAuthProperty.getTokenFiledName();
        switch (webAuthProperty.getTokenSource()) {
            case BODY:
                String body = getBody(request);
                AssertUtil.hasText(body, "请求体空,未找到Token");
                Map<String, Object> map = JSON.parseObject(body, Map.class);
                token = map.get(webAuthProperty.getTokenFiledName()).toString();
                sourceValue = body;
                break;
            case COOKIE:
                sourceValue = getFromCookie(request, webAuthProperty.getTokenFiledName());
                AssertUtil.hasText(sourceValue, "未找到Token");
                token = sourceValue;
                break;
            case HEADER:
                sourceValue = request.getHeader(webAuthProperty.getTokenFiledName());
                AssertUtil.hasText(sourceValue, "未找到Token");
                token = sourceValue;
                break;
            case PARAM:
                sourceValue = getFromParam(request, webAuthProperty.getTokenFiledName());
                AssertUtil.hasText(sourceValue, "未找到Token");
                token = sourceValue;
                break;
            default:
                AssertUtil.throwError(() -> new NotFoundTokenException("未找到Token"));
        }
        Date createDate = new Date();
        return SimpleTokenModel.create(token, webAuthProperty.getTokenSource(), sourceField, sourceValue,
            DateUtil.offset(createDate, DateField.SECOND, webAuthProperty.getTokenExpireTime()));
    }
}
