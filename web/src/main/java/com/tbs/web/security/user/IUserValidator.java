package com.tbs.web.security.user;

import com.tbs.common.exception.FormattedRuntimeException;
import org.springframework.core.Ordered;
import org.springframework.security.core.userdetails.UserDetails;

import javax.security.auth.login.LoginException;

/**
 * 用户验证接口 用于验证用户
 */
public interface IUserValidator extends Ordered {
    /**
     * 验证用户
     *
     * @param userDetails 用户信息
     * @return 验证结果
     */
    void validate(UserDetails userDetails) throws FormattedRuntimeException;

}
