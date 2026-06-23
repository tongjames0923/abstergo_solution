package com.tbs.shared.interfaces.constraint.impl;

import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.interfaces.constraint.IUserNamePasswordValidator;

public class NoUserNameAndPasswordValidator implements IUserNamePasswordValidator {
    @Override
    public boolean validateUserName(String username) {
        if (StrUtil.isEmpty(username)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean validatePassword(String password) {
        if (StrUtil.isEmpty(password)) {
            return false;
        }
        return true;
    }
}
