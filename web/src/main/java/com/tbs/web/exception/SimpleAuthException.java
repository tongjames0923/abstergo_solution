package com.tbs.web.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.IExceptionLevel;
import org.springframework.security.core.AuthenticationException;

/**
 * @author abstergo
 */
public class SimpleAuthException extends AuthenticationException implements IExceptionLevel {
    public SimpleAuthException(String msg) {
        super(msg);
    }

    public SimpleAuthException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public ErrorLevelEnum getLevel() {
        return ErrorLevelEnum.USER_INPUT_ERROR;
    }
}
