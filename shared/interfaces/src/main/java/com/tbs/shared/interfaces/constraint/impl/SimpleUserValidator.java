package com.tbs.shared.interfaces.constraint.impl;

import com.tbs.shared.interfaces.constraint.IUserNamePasswordValidator;
import com.tbs.common.util.StrUtil;

/**
 * 默认用户名密码校验器
 *
 * @author abstergo
 */
public class SimpleUserValidator implements IUserNamePasswordValidator {
    @Override
    public boolean validateUserName(String username) {
        if (StrUtil.isEmpty(username)) {
            return false;
        }
        return username.matches("^[a-zA-Z0-9_]{6,16}$");
    }

    @Override
    public boolean validatePassword(String password) {
        if (StrUtil.isEmpty(password)) {
            return false;
        }
        String strongPasswordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,20}$";
        return password.matches(strongPasswordRegex);
    }

    @Override
    public boolean validateNickName(String nickName) {
        if (StrUtil.isEmpty(nickName)) {
            return false;
        }
        String chineseNicknameRegex = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]{2,20}$";
        return nickName.matches(chineseNicknameRegex);
    }
}
