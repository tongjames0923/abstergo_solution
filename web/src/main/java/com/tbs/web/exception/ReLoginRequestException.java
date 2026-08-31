package com.tbs.web.exception;

import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.IExceptionLevel;
import org.springframework.security.core.AuthenticationException;

/**
 * 重新登录请求异常（抽象基类）
 * <p>
 * 表示「当前会话已失效、需要客户端重新登录」的语义异常。继承 {@link AuthenticationException} 以接入 Spring Security 认证链，
 * 并实现 {@link IExceptionLevel} 标记为业务逻辑级错误。具体场景（如令牌过期、被踢下线）由子类携带不同 message。
 * </p>
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
