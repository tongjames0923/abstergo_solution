package com.tbs.app.account.annotation;

import java.lang.annotation.*;

/**
 * 用户状态检查容器注解
 * <p>
 * 当需要对同一个方法应用多个 {@link UserStatusCheck} 注解时，
 * 使用此容器注解来承载多个 {@link UserStatusCheck}。
 * </p>
 * <p>
 * 注意：此注解通常由 {@link UserStatusCheck} 的 {@link Repeatable} 元注解自动使用，
 * 开发者一般不需要直接使用此注解。
 * </p>
 *
 * @author abstergo
 * @see UserStatusCheck
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Inherited
@Documented
public @interface UserStatusChecks {

    /**
     * 用户状态检查注解数组
     *
     * @return 用户状态检查注解数组
     */
    UserStatusCheck[] value();
}
