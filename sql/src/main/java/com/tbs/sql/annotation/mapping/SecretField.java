package com.tbs.sql.annotation.mapping;

import java.lang.annotation.*;

/**
 * 安全字段标记注解
 *
 * @author abstergo
 * @since 1.0.0
 */
@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface SecretField {
}
