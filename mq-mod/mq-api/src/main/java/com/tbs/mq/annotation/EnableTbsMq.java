package com.tbs.mq.annotation;

import com.tbs.mq.config.MqPackAutoRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用MQ抽象层默认装配的注解
 * <p>
 * 在Spring Boot应用中使用此注解即可快速启动MQ重试链路的厂商无关默认组件
 * （重试延迟策略、失败消息存储兜底、重投递调度器）。
 * 具体MQ厂商模块（如rabbitmq-impl的@EnableRabbitMq）已包含本注解的装配范围，
 * 单独使用本注解适用于仅引入抽象层与存储实现的场景。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Import(MqPackAutoRegister.class)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface EnableTbsMq {
}
