package com.tbs.logging.v2;

import com.tbs.logging.form.LoggingRecordForm;
import com.tbs.rabbitmq.v2.RabbitMqMessageProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * 日志记录消息分发器（MQ v2 记录驱动模型 · 分发段的日志负载落地）
 * <p>
 * 分发逻辑（按负载类型 {@link LoggingRecordForm} 回查消费者、广播消费、逐条构建 SUCCESS/FAIL 台账）与
 * 厂商无关，全部由父类 {@link RabbitMqMessageProcessor} 承担；本类仅把传输层固定为 {@code Message}、
 * 负载类型固定为 {@link LoggingRecordForm}，使其可注入到 {@code R=Message, T=LoggingRecordForm} 的日志消费上下文。
 * </p>
 * <p>
 * 非 Spring 注解类，由日志模块 config 以 {@code @Bean} 装配；父类声明的 {@code IConsumerRegistry} 字段
 * 在 {@code @Bean} 实例上由容器按类型注入（内存注册表 Bean）。
 * </p>
 *
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public class LoggingRecordMessageProcessor extends RabbitMqMessageProcessor<LoggingRecordForm> {
}
