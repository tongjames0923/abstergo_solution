package com.tbs.web.exception;

import com.tbs.common.exception.FormattedRuntimeException;

/**
 * 找不到Token异常
 *
 * @author tongj
 */
public class NotFoundTokenException extends FormattedRuntimeException {

    public NotFoundTokenException(String message, Object... params) {
        super(message, params);
    }
}
