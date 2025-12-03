package com.tbs.shared.interfaces.constraint;

import com.tbs.shared.interfaces.annoation.PasswordValid;

import javax.annotation.Resource;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author abstergo
 */

public class PasswordValidator implements ConstraintValidator<PasswordValid, String> {

    @Resource
    private IUserNamePasswordValidator userNamePasswordValidator;

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return userNamePasswordValidator.validatePassword(s);
    }
}
