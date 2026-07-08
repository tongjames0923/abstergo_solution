package com.tbs.app.account.annotation;

import com.tbs.app.account.impl.OnlyEnabledAccount;

import java.lang.annotation.*;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Inherited
@Documented
@UserStatusCheck(validator = OnlyEnabledAccount.class)
public @interface OnlyActiveAccountUse {
}
