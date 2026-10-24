package com.tbs.app.config;

import com.tbs.broadcast.publisher.impl.SimpleDistributedAcceptedPublishResult;
import com.tbs.mq.v2.core.IMqMessageProcessor;
import com.tbs.mq.v2.core.IMqMessageReceiver;
import com.tbs.mq.v2.core.IMqRecorder;
import com.tbs.rabbitmq.v2.RabbitMqMessageProcessor;
import com.tbs.rabbitmq.v2.RabbitMqMessageReceiver;
import com.tbs.redis.mq.RedisMqRecorder;
import org.springframework.amqp.core.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分布式广播 v2 消费协作装配（配合 {@code SimpleRabbitMqSupport} 的 v2 上下文形态）
 * <p>
 * {@code AbstractRabbitMqSupport} 已迁移为 v2 {@code RabbitMqContext}：监听容器与接收→建档→分发
 * 全链路由其父类自装配，本 config 仅补广播负载类型所需的记录器/接收器/分发器协作 Bean；
 * 单飞抢占器（Redis 实现）与消费者注册表（内存实现）复用 {@link CdcV2ConsumerConfig} 的单例
 * （按 contextId/负载类型天然隔离）。
 * </p>
 *
 * @author tongj
 * @since 2.0.0
 */
@Configuration
public class BroadcastV2ConsumerConfig {

    /**
     * 广播消息记录器（Redis 实现，作用域按广播上下文ID隔离）
     *
     * @return 记录器，主记录与台账落 Redis
     */
    @Bean
    public IMqRecorder<Message, SimpleDistributedAcceptedPublishResult> broadcastRecordRecorder() {
        return new RedisMqRecorder<>();
    }

    /**
     * 广播消息接收器（接收段），绑定 Redis 记录器
     *
     * @param broadcastRecordRecorder 广播记录器
     * @return 接收器
     */
    @Bean
    public IMqMessageReceiver<Message, SimpleDistributedAcceptedPublishResult> broadcastRecordReceiver(
        IMqRecorder<Message, SimpleDistributedAcceptedPublishResult> broadcastRecordRecorder) {
        return new BroadcastMessageReceiver(broadcastRecordRecorder);
    }

    /**
     * 广播消息分发器（分发段），消费者注册表由父类字段按类型注入
     *
     * @return 分发器
     */
    @Bean
    public IMqMessageProcessor<Message, SimpleDistributedAcceptedPublishResult> broadcastRecordProcessor() {
        return new BroadcastMessageProcessor();
    }

    /**
     * 广播接收器薄壳：仅绑定传输层 Message 与广播负载类型，接收编排由父类承担
     */
    static class BroadcastMessageReceiver
        extends RabbitMqMessageReceiver<SimpleDistributedAcceptedPublishResult> {
        BroadcastMessageReceiver(IMqRecorder<Message, SimpleDistributedAcceptedPublishResult> recorder) {
            super(recorder);
        }
    }

    /**
     * 广播分发器薄壳：分发逻辑厂商无关，仅固定负载类型使泛型可注入
     */
    static class BroadcastMessageProcessor
        extends RabbitMqMessageProcessor<SimpleDistributedAcceptedPublishResult> {
    }
}
