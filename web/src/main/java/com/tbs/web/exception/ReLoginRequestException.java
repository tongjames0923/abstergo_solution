package com.tbs.web.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 重新登录请求异常
 *
 * @author tongj
 */
public abstract class ReLoginRequestException extends AuthenticationException {
    public ReLoginRequestException(String message) {
        super(message);
    }
}
