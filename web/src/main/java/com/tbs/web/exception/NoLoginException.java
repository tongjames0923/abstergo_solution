package com.tbs.web.exception;

/**
 * 未登录异常
 *
 * @author tongj
 */
public class NoLoginException extends RuntimeException {
    public NoLoginException(String message) {
        super(message);
    }
}
