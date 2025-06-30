package com.tbs.web.exception;

import com.tbs.common.exception.FormattedRuntimeException;

/**
 * 令牌已过期异常
 *
 * @author tbs
 */
public class InvalidTokenException extends FormattedRuntimeException {
    /**
     * 构造方法
     */
    public InvalidTokenException(String message, Object... params) {
        super(message, params);
    }
}
