package com.tbs.web.security.user.impl;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.web.security.user.IUserLoginInfoService;
import com.tbs.web.security.user.IUserValidator;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;

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
