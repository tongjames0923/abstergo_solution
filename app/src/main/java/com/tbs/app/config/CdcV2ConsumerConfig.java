package com.tbs.app.config;

import com.tbs.cdc.model.CdcEvent;
import com.tbs.mq.v2.consume.IConsumerRegistry;
import com.tbs.mq.v2.consume.IMqConsumer;
import com.tbs.mq.v2.core.IMqMessageMonopolizer;
import com.tbs.mq.v2.core.IMqMessageProcessor;
import com.tbs.mq.v2.core.IMqMessageReceiver;
import com.tbs.mq.v2.core.IMqRecorder;
import com.tbs.mq.v2.impl.InMemoryConsumerRegistry;
import com.tbs.rabbitmq.support.MqConsumerProperties;
import com.tbs.rabbitmq.v2.RabbitMqContext;
import com.tbs.rabbitmq.v2.RabbitMqMessageProcessor;
import com.tbs.rabbitmq.v2.RabbitMqMessageReceiver;
import com.tbs.redis.mq.RedisMqMessageMonopolizer;
import com.tbs.redis.mq.RedisMqRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * CDC 事件 v2 消费链路装配（替代 v1 {@code AbstractMqConsumer} 直连消费）
 * <p>
 * 按 v2 记录驱动模型组装接收→台账→分发全链路，消费队列 {@code cdc.erp.e10}（交换机与绑定由
 * cdc-service 生产端声明，本侧仅挂队列，与 v1 形态一致）。记录器与单飞抢占器选用 Redis 实现
 * （多实例共享主记录/台账、跨实例单飞），消费者注册表仍为进程内实现（路由对象无法跨实例序列化）；
 * 协作者均刻意不加 {@code @Component}，集中由本 config 装配；消费动作为 log 输出（沿用原 {@code sqlServerConsumer} 行为），
 * 业务消费者实现 {@link IMqConsumer} 注册进注册表即可替换。
 * </p>
 *
 * @author tongj
 * @since 2.0.0
 */
@Configuration
public class CdcV2ConsumerConfig {

    /**
     * 消费上下文ID：记录层按此隔离本队列的消息与台账归属，全局唯一（日志上下文占用 2）
     */
    private static final Long CDC_CONTEXT_ID = 3L;

    /**
     * CDC 消息记录器（Redis 实现，可靠性真源）
     *
     * @return 记录器，主记录与台账落 Redis，作用域按上下文ID隔离
     */
    @Bean
    public IMqRecorder<Message, CdcEvent> cdcRecordRecorder() {
        return new RedisMqRecorder<>();
    }

    /**
     * CDC 消息单飞抢占器（Redis 实现，保证同一消息在集群内不被并行重处理）
     *
     * @return 抢占器，按上下文ID + messageId 加分布式锁
     */
    @Bean
    public IMqMessageMonopolizer cdcRecordMonopolizer() {
        return new RedisMqMessageMonopolizer();
    }

    /**
     * CDC 消费者注册表（内存实现，按负载类型路由消费者）
     *
     * @return 注册表
     */
    @Bean
    public IConsumerRegistry cdcConsumerRegistry() {
        return new InMemoryConsumerRegistry();
    }

    /**
     * CDC 消息接收器（接收段），绑定内存记录器
     *
     * @param cdcRecordRecorder CDC 记录器
     * @return 接收器
     */
    @Bean
    public IMqMessageReceiver<Message, CdcEvent> cdcRecordReceiver(
        IMqRecorder<Message, CdcEvent> cdcRecordRecorder) {
        return new CdcMessageReceiver(cdcRecordRecorder);
    }

    /**
     * CDC 消息分发器（分发段），消费者注册表由父类字段按类型注入
     *
     * @return 分发器
     */
    @Bean
    public IMqMessageProcessor<Message, CdcEvent> cdcRecordProcessor() {
        return new CdcMessageProcessor();
    }

    /**
     * CDC 事件消费者，创建即注册进注册表，供分发段按负载类型回查
     *
     * @param cdcConsumerRegistry 消费者注册表
     * @return CDC 消费者
     */
    @Bean
    public IMqConsumer<CdcEvent> cdcEventConsumer(IConsumerRegistry cdcConsumerRegistry) {
        CdcEventConsumer consumer = new CdcEventConsumer();
        cdcConsumerRegistry.register(consumer, CdcEvent.class);
        return consumer;
    }

    /**
     * CDC v2 消费上下文：自装配监听容器 + 建档确认全链路
     *
     * @return 消费上下文
     */
    @Bean
    @DependsOn("cdcEventConsumer")
    public CdcRabbitMqContext cdcRecordContext() {
        return new CdcRabbitMqContext();
    }

    /**
     * CDC 接收器薄壳：仅绑定传输层 Message 与负载 CdcEvent，接收编排全部由父类承担
     */
    static class CdcMessageReceiver extends RabbitMqMessageReceiver<CdcEvent> {
        CdcMessageReceiver(IMqRecorder<Message, CdcEvent> recorder) {
            super(recorder);
        }
    }

    /**
     * CDC 分发器薄壳：分发逻辑厂商无关，仅固定负载类型使泛型可注入
     */
    static class CdcMessageProcessor extends RabbitMqMessageProcessor<CdcEvent> {
    }

    /**
     * CDC 消费上下文：沿用 v1 的队列名，交换机/绑定由 cdc-service 生产端声明，本侧不重复声明
     */
    static class CdcRabbitMqContext extends RabbitMqContext<CdcEvent> {

        @Override
        public Class<CdcEvent> getMessagePayloadType() {
            return CdcEvent.class;
        }

        @Override
        public Long getContextId() {
            return CDC_CONTEXT_ID;
        }

        @Override
        protected MqConsumerProperties getMqConsumerProperties() {
            return MqConsumerProperties.builder("cdc.erp.e10").prefetchCount(250).build();
        }
    }

    /**
     * CDC 事件消费者：消费动作沿用原 v1 {@code sqlServerConsumer} 的日志输出
     */
    @Slf4j
    static class CdcEventConsumer implements IMqConsumer<CdcEvent> {

        @Override
        public String businessId() {
            return "cdc";
        }

        @Override
        public Integer consumeId() {
            return 1;
        }

        @Override
        public void consume(CdcEvent message) {
            log.info("Received message: {}", message);
        }
    }
}
