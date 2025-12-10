package com.tbs.rabbitmq.annotation;

import com.tbs.rabbitmq.config.RabbitMqAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用RabbitMQ功能的注解
 * <p>
 * 在Spring Boot应用中使用此注解可以自动启用和配置RabbitMQ功能
 * 通过@Import引入RabbitMqAutoRegister类，实现RabbitMQ相关组件的自动注册
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Import(RabbitMqAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface EnableRabbitMq {
}
