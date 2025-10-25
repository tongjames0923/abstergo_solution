package com.tbs.shared.interfaces.constraint;

import javax.annotation.Resource;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.stereotype.Component;

import com.tbs.shared.interfaces.annoation.PasswordValid;

/**
 * @author abstergo
 */
@Component
public class PasswordValidator implements ConstraintValidator<PasswordValid, String> {

    @Resource
    private IUserNamePasswordValidator userNamePasswordValidator;

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return userNamePasswordValidator.validatePassword(s);
    }
}
