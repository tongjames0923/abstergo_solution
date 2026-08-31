package com.tbs.web.security.user.impl;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.web.security.user.IUserLoginInfoService;
import com.tbs.web.security.user.IUserValidator;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;

/**
 * 登录次数验证器
 * <p>
 * {@link IUserValidator} 的占位实现，当前未实现具体校验逻辑（方法体为空），按 {@code order=0} 位列校验链首位。
 * 预留用于扩展「登录失败次数限制 / 同端并发登录限制」等策略。
 * </p>
 */
public class UserLoginCountValiadtor implements IUserValidator {

    @Resource
    IUserLoginInfoService loginInfoService;

    @Override
    public void validate(UserDetails userDetails) throws FormattedRuntimeException {

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
