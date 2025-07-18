package com.tbs.app.constraint;

import com.tbs.app.annoation.UserNameValid;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author abstergo
 */
@Component
public class UserNameValidator implements ConstraintValidator<UserNameValid, String> {

    @Resource
    private IUserNamePasswordValidator validator;

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return validator.validateUserName(s);
    }
}
