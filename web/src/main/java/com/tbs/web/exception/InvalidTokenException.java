package com.tbs.web.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.IExceptionLevel;
import org.springframework.security.core.AuthenticationException;

/**
 * 令牌已过期异常
 *
 * @author tbs
 */
public class InvalidTokenException extends ReLoginRequestException  {

    public InvalidTokenException(String msg) {
        super(msg);
    }


}
