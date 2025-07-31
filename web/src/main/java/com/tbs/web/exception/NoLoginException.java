package com.tbs.web.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 未登录异常
 *
 * @author tongj
 */
public class NoLoginException extends AuthenticationException {
    public NoLoginException(String message) {
        super(message);
    }
}
