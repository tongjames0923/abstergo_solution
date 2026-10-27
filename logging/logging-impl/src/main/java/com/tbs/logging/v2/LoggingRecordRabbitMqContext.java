package com.tbs.logging.v2;

import com.tbs.logging.form.LoggingRecordForm;
import com.tbs.rabbitmq.support.MqConsumerProperties;
import com.tbs.rabbitmq.v2.RabbitMqContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 日志记录消费上下文（MQ v2 记录驱动模型 · 容器推送/原生批量监听形态的日志落地）
 * <p>
 * 复用 {@link RabbitMqContext} 的自装配监听容器 + 建档确认全链路，负载类型固定为 {@link LoggingRecordForm}。
 * 存量 v1 消费者（{@code DbLogWriter}）已随 v2 全面替换下线，本上下文直接接管原队列 {@code systemLog}
 * （交换机 {@code tbs.log.queue} / 路由键 {@code systemLog} 均不变，投递端无需改动）；
 * 投递端携带 payloadType/payloadFormat/contentType 头，接收段走 v2 原生负载转换器精确反序列化。
 * </p>
 * <p>
 * 非 Spring 注解类，由日志模块 config 以 {@code @Bean} 装配；父类的协作 Bean（receiver/processor/recorder/
 * monopolizer）与传输层依赖（ConnectionFactory/AmqpAdmin）按类型注入。记录器与单飞抢占器采用 Redis 协作实现，
 * 主记录/台账跨实例共享且重启不丢，与 CDC、广播链路共用同一套 MQ v2 协作件。
 * </p>
 *
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public class LoggingRecordRabbitMqContext extends RabbitMqContext<LoggingRecordForm> {

    /**
     * 消费队列名：v1 消费者已下线，直接接管原 {@code systemLog} 队列，无并行、无双写
     */
    private static final String QUEUE_NAME = "systemLog";

    /**
     * 沿用既有交换机，不改动生产端
     */
    private static final String EXCHANGE_NAME = "tbs.log.queue";

    /**
     * 路由键，与投递端保持一致
     */
    private static final String ROUTING_KEY = "systemLog";

    /**
     * 上下文ID：记录层按此隔离本队列的消息与台账归属，全局唯一
     */
    private static final Long CONTEXT_ID = 2L;

    /**
     * 业务负载类型，消费者注册与分发回查的路由键
     *
     * @return {@link LoggingRecordForm} 类型
     */
    @Override
    public Class<LoggingRecordForm> getMessagePayloadType() {
        return LoggingRecordForm.class;
    }

    /**
     * 上下文（队列级）身份标识
     *
     * @return 上下文ID
     */
    @Override
    public Long getContextId() {
        return CONTEXT_ID;
    }

    /**
     * 提供消费传输层配置：接管原队列 {@code systemLog}，复用既有交换机/路由键，手动 ack（父类默认），保持单条投递语义
     *
     * @return 消费者传输配置
     */
    @Override
    protected MqConsumerProperties getMqConsumerProperties() {
        return MqConsumerProperties.builder(QUEUE_NAME).exchange(EXCHANGE_NAME).routingKey(ROUTING_KEY)
            .prefetchCount(250).build();
    }
}
