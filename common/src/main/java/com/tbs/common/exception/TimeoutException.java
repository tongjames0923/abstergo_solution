package com.tbs.common.exception;

/**
 * 超时异常
 *
 * @author abstergo
 */
public class TimeoutException extends FormattedRuntimeException {
    public TimeoutException(String message, Object... params) {
        super(message, params);
    }
}
