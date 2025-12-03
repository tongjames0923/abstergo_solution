package com.tbs.shared.interfaces.constraint;

import com.tbs.shared.interfaces.annoation.UserNameValid;

import javax.annotation.Resource;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author abstergo
 */

public class UserNameValidator implements ConstraintValidator<UserNameValid, String> {

    @Resource
    private IUserNamePasswordValidator validator;

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return validator.validateUserName(s);
    }
}
