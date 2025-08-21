package com.tbs.web.security.token;

import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.exception.NotFoundTokenException;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

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
    Optional<ITokenModel> pick(@NotNull HttpServletRequest request) throws NotFoundTokenException;
}
