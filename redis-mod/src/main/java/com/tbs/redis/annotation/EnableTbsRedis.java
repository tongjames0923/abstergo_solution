package com.tbs.redis.annotation;

import com.tbs.redis.config.RedisPackAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Import(RedisPackAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface EnableTbsRedis {
}
