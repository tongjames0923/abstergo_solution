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

    @Override
    public ErrorLevelEnum getLevel() {
        return ErrorLevelEnum.USER_INPUT_ERROR;
    }
}
