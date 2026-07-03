package com.tbs.web.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.IExceptionLevel;
import org.springframework.security.core.AuthenticationException;

/**
 * 重新登录请求异常
 *
 * @author tongj
 */
public abstract class ReLoginRequestException extends AuthenticationException implements IExceptionLevel {
    public ReLoginRequestException(String message) {
        super(message);
    }

    public ReLoginRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public ErrorLevelEnum getLevel() {
        return ErrorLevelEnum.BUSINESS_LOGIC_ERROR;
    }
}
