package com.tbs.app.account.exception;

import com.tbs.common.exception.FormattedRuntimeException;

/**
 * 账户异常错误
 *
 **/
public class UnNormalAccountStatusException extends FormattedRuntimeException {
    public UnNormalAccountStatusException(String message) {
        super(message);
    }

    public UnNormalAccountStatusException(String message, Object... params) {
        this(null, message, params);
    }

    public UnNormalAccountStatusException(Throwable cause, String message, Object... params) {
        super(cause, message, params);
    }

    public UnNormalAccountStatusException() {
        super("账户异常错误");
    }
}
