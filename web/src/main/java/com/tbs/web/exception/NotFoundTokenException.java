package com.tbs.web.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 找不到Token异常
 *
 * @author tongj
 */
public class NotFoundTokenException extends AuthenticationException {

    public NotFoundTokenException(String msg) {
        super(msg);
    }

    public NotFoundTokenException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
