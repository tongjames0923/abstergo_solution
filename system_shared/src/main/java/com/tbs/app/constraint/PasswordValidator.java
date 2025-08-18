package com.tbs.app.constraint;

import com.tbs.app.annoation.PasswordValid;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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
