package com.tbs.web.exception;

/**
 * 找不到Token异常
 *
 * @author tongj
 */
public class NotFoundTokenException extends ReLoginRequestException {

    public NotFoundTokenException(String msg) {
        super(msg);
    }

    public NotFoundTokenException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
