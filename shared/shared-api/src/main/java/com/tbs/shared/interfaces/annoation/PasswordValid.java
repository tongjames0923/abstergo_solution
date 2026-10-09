package com.tbs.shared.interfaces.annoation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;

import com.tbs.shared.interfaces.constraint.PasswordValidator;

@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordValid {
    String message() default "密码格式错误";

    Class<?>[] groups() default {};

    Class<? extends java.lang.annotation.Annotation>[] payload() default {};
}
