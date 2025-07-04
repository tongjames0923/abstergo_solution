package com.tbs.web.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author abstergo
 */
public class SimpleAuthException extends AuthenticationException {
    public SimpleAuthException(String msg) {
        super(msg);
    }
}
