package com.tbs.rabbitmq.annotation;

import com.tbs.rabbitmq.config.RabbitMqAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Import(RabbitMqAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface EnableRabbitMq {
}
