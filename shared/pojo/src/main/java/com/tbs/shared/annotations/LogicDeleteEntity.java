package com.tbs.shared.annotations;

import com.tbs.shared.constant.CommonConstant;

import java.lang.annotation.*;

/**
 * @author abstergo
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface LogicDeleteEntity {
    int deleteFlagValue() default CommonConstant.FLAG_OFF;

    String filedName() default "flag";
}
