package com.tbs.web.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.IExceptionLevel;
import org.springframework.security.core.AuthenticationException;

/**
 * 未登录异常
 *
 * @author tongj
 */
public class NoLoginException extends AuthenticationException implements IExceptionLevel {
    public NoLoginException(String message) {
        super(message);
    }

    @Override
    public ErrorLevelEnum getLevel() {
        return ErrorLevelEnum.BUSINESS_LOGIC_ERROR;
    }
}
