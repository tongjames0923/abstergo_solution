package com.tbs.app.config.condition;

import com.tbs.app.config.property.TestControllerMode;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * 测试控制器模式条件注解
 * <p>
 * 当 app.gateway.enable-test-controller 的值匹配指定的任意一个模式时，条件成立，Bean 被注册。
 * 支持多值"或"语义，例如 {@code @ConditionalOnTestControllerMode({ALL, PUB})} 表示
 * 配置为 all 或 pub 时均激活。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(TestControllerModeCondition.class)
public @interface ConditionalOnTestControllerMode {

    /**
     * 允许激活的控制器模式（任意一个匹配即生效）
     */
    TestControllerMode[] value();
}
