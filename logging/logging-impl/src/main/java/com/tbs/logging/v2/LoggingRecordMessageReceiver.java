package com.tbs.logging.v2;

import com.tbs.logging.form.LoggingRecordForm;
import com.tbs.mq.v2.core.IMqRecorder;
import com.tbs.rabbitmq.v2.RabbitMqMessageReceiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;

/**
 * 日志记录消息接收器（MQ v2 记录驱动模型 · 接收段的日志负载落地）
 * <p>
 * 继承 {@link RabbitMqMessageReceiver}：传输层固定为 Spring AMQP 的 {@link Message}，负载类型为
 * {@link LoggingRecordForm}。接收段的组装/校验/建档/配对/确认编排、以及经 MessageConverter 反序列化
 * 负载、按消息头 businessId（缺失时回落队列名）解析业务标识，均由父类完成——日志生产者未写 businessId
 * 头时，业务标识自动回落为 v2 队列名，非空即可通过建档校验，无需本类覆盖任何钩子。
 * </p>
 * <p>
 * 非 Spring 注解类，由日志模块 config 以 {@code @Bean} 装配（recorder 经构造器传入），保持与内存协作实现一致的
 * "注册交 config 层"约定。
 * </p>
 *
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public class LoggingRecordMessageReceiver extends RabbitMqMessageReceiver<LoggingRecordForm> {

    /**
     * 构造日志记录消息接收器
     *
     * @param recorder 消息记录器，接收段建档的执行方（内存实现由 config 注入）
     */
    public LoggingRecordMessageReceiver(IMqRecorder<Message, LoggingRecordForm> recorder) {
        super(recorder);
    }
}
