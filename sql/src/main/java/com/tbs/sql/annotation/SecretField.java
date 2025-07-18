package com.tbs.sql.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author tongj
 */
@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface SecretField {
}
