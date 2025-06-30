package com.tbs.web.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 令牌已过期异常
 *
 * @author tbs
 */
public class InvalidTokenException extends AuthenticationException {

    public InvalidTokenException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public InvalidTokenException(String msg) {
        super(msg);
    }
}
