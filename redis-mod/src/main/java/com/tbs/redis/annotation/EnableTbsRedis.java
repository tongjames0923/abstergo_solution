package com.tbs.redis.annotation;

import com.tbs.redis.config.RedisPackAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用TBS Redis功能的注解
 * <p>
 * 在Spring Boot应用中使用此注解可以自动启用和配置TBS Redis功能
 * 通过@Import引入RedisPackAutoRegister类，实现Redis相关组件的自动注册
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Import(RedisPackAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface EnableTbsRedis {
}
