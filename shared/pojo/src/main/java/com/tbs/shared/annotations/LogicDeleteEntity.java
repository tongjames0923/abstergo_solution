package com.tbs.shared.annotations;

import java.lang.annotation.*;

/**
 * @author abstergo
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface LogicDeleteEntity {
    int deleteFlagValue() default 0;

    String filedName();
}
