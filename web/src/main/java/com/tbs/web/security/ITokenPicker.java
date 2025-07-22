package com.tbs.web.security;

import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.exception.NotFoundTokenException;

import javax.servlet.http.HttpServletRequest;

/**
 * 获取用户登录Token 的接口
 *
 * @author tongj
 */
public interface ITokenPicker {
    /**
     * 获取用户登录Token
     *
     * @param request 请求
     * @return
     * @throws NotFoundTokenException 找不到Token
     * @throws InvalidTokenException  过期Token
     */
    ITokenModel pick(HttpServletRequest request) throws NotFoundTokenException;
}
