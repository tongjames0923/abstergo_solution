package com.tbs.logging.v2;

import com.tbs.logging.form.LoggingRecordForm;
import com.tbs.rabbitmq.support.MqConsumerProperties;
import com.tbs.rabbitmq.v2.RabbitMqContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 日志记录消费上下文（MQ v2 记录驱动模型 · 容器推送/原生批量监听形态的日志落地）
 * <p>
 * 复用 {@link RabbitMqContext} 的自装配监听容器 + 建档确认全链路，负载类型固定为 {@link LoggingRecordForm}。 与存量 v1 消费者（{@code DbLogWriter}，队列
 * {@code systemLog}）并行运行：本上下文绑定一条<b>独立队列</b> {@code systemLog.v2} 到<b>同一交换机</b>{@code tbs.log.queue}
 * 的<b>同一路由键</b>{@code systemLog}。 直连交换机对同一路由键的多条队列绑定是"复制投递"语义——日志生产者每次发送会同时进入 v1 与 v2 两条队列， 各自独立消费、互不抢占，故 v1 行为完全不受影响。
 * </p>
 * <p>
 * 非 Spring 注解类，由日志模块 config 以 {@code @Bean} 装配；父类的协作 Bean（receiver/processor/recorder/
 * monopolizer）与传输层依赖（ConnectionFactory/AmqpAdmin）按类型注入。记录层采用内存协作实现，进程重启即清空， 定位为链路验证/开发环境用；生产替换为 DB 记录器即可，本上下文无需改动。
 * </p>
 *
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public class LoggingRecordRabbitMqContext extends RabbitMqContext<LoggingRecordForm> {

    /**
     * v2 并行消费队列名：独立于 v1 的 {@code systemLog}，绑定同一交换机与路由键以获取复制投递
     */
    private static final String V2_QUEUE_NAME = "systemLog";

    /**
     * 复用 v1 的既有交换机与路由键，仅新增独立队列，不改动生产端
     */
    private static final String EXCHANGE_NAME = "tbs.log.queue";

    /**
     * 路由键，与 v1 保持一致以便同一份日志被复制到 v2 队列
     */
    private static final String ROUTING_KEY = "systemLog";

    /**
     * 上下文ID：记录层按此隔离 v2 队列的消息与台账归属，全局唯一
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
     * 提供 v2 并行消费的传输层配置：独立队列 + 复用交换机/路由键，手动 ack（父类默认），保持单条投递语义
     *
     * @return 消费者传输配置
     */
    @Override
    protected MqConsumerProperties getMqConsumerProperties() {
        return MqConsumerProperties.builder(V2_QUEUE_NAME).exchange(EXCHANGE_NAME).routingKey(ROUTING_KEY)
            .prefetchCount(250).build();
    }
}
