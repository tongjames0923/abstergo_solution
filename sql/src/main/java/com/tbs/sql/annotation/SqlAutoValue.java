package com.tbs.sql.annotation;

import com.tbs.sql.provider.IAutoValueProvider;
import org.springframework.stereotype.Component;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * SQL自动填充值注解
 * <p>
 * 用于标记需要在SQL操作前自动填充值的字段
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface SqlAutoValue {
    /**
     * 自动值提供者类
     * <p>
     * 用于提供自动填充的值
     * </p>
     *
     * @return 自动值提供者类
     */
    Class<? extends IAutoValueProvider> value() default IAutoValueProvider.class;

}
