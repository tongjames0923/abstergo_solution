package com.tbs.rabbitmq.support;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import lombok.Data;
import org.springframework.amqp.core.AcknowledgeMode;

import java.time.Duration;

/**
 * MQ消费者配置属性
 * <p>
 * 集中管理 v2 消费上下文（{@code RabbitMqContext} 自建容器接入）的所有可配置传输维度，支持通过Builder模式按实例独立配置。 每个属性均有合理默认值，子类只需覆盖关心的配置项。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Data
public class MqConsumerProperties {

    // ======================== 队列与交换机 ========================

    /**
     * 队列名称（必填）
     */
    private String queueName;

    /**
     * 交换机名称，为null时不声明交换机
     */
    private String exchangeName;

    /**
     * 路由键，为null时不创建绑定关系
     */
    private String routingKey;

    /**
     * 交换机类型
     */
    private ExchangeType exchangeType = ExchangeType.DIRECT;

    // ======================== 队列属性 ========================

    /**
     * 是否持久化队列
     */
    private boolean durable = true;

    /**
     * 是否排他队列，仅对当前连接可见
     */
    private boolean exclusive = false;

    /**
     * 是否自动删除队列
     */
    private boolean autoDelete = false;

    /**
     * 是否自动声明队列、交换机和绑定关系
     */
    private boolean autoDeclare = true;

    // ======================== 消费者行为 ========================

    /**
     * ACK模式，默认手动确认
     */
    private AcknowledgeMode acknowledgeMode = AcknowledgeMode.MANUAL;

    /**
     * 消费者并发度，格式 "min-max"，如 "1-5"
     */
    private String concurrency = "1-1";

    /**
     * 预取数量，控制每个消费者在确认前可预取的消息数，0表示不限制
     */
    private int prefetchCount = 250;

    /**
     * 是否启用消费端批量拉取（对接 Spring AMQP 原生 batch listener）
     * <p>
     * 开启后容器在单个消费线程内按 {@link #batchSize} 收集消息，一次性回调批量接收入口完成批量建档； 逐条确认语义不变。默认关闭，保持单条投递行为。
     * </p>
     */
    private boolean consumerBatchEnabled = false;

    /**
     * 单批最大消息数，仅在 {@link #consumerBatchEnabled} 为 true 时生效
     */
    private int batchSize = 1;

    /**
     * 单批最大等待时间，单位毫秒，0表示不限制
     */
    private long batchReceiveTimeout = 1500L;

    /**
     * 是否在PostConstruct阶段自动启动消费者
     */
    private boolean autoStart = true;

    // ======================== 重试策略（v1 遗留字段：v2 链路不使用，重试恢复走记录层） ========================

    /**
     * 是否启用重试机制
     * <p>
     * v1 遗留字段：v2 记录驱动模型不再消费此项（失败恢复基于自建落库记录，不依赖厂商重投递）。
     * </p>
     */
    private boolean retryEnabled = true;

    /**
     * 最大重试次数
     * <p>
     * v1 遗留字段：v2 链路不消费，重试上限由记录层重试扫描组件裁决。
     * </p>
     */
    private int maxRetryCount = 3;

    // ======================== 幂等控制（v1 遗留字段：v2 以建档去重与消费者幂等契约取代） ========================

    /**
     * 是否启用消费幂等
     * <p>
     * v1 遗留字段：v2 链路不再使用独立幂等存储，接收段以 messageId 建档去重、消费者按契约保证幂等。
     * </p>
     */
    private boolean idempotentEnabled = false;

    /**
     * 幂等键有效期
     * <p>
     * v1 遗留字段：v2 链路不消费。
     * </p>
     */
    private Duration idempotentTtl;

    // ======================== 死信与TTL ========================

    /**
     * 是否启用死信留痕
     * <p>
     * 开启后自动声明死信交换机与死信队列（默认命名为“队列名.dlx/队列名.dlq”），
     * 消费失败且不再重试的消息将转入死信队列留痕。
     * </p>
     */
    private boolean deadLetterEnabled = false;

    /**
     * 业务性失败（不重试）是否同样转入死信留痕，false则直接丢弃
     */
    private boolean deadLetterOnBusinessError = true;

    /**
     * 死信队列名称，为null时默认为“队列名.dlq”
     */
    private String deadLetterQueue;

    /**
     * 死信交换机名称，为null且deadLetterEnabled开启时默认为“队列名.dlx”
     */
    private String deadLetterExchange;

    /**
     * 死信路由键
     */
    private String deadLetterRoutingKey;

    /**
     * 消息TTL（毫秒），0表示不过期
     */
    private long messageTtl = 0;

    /**
     * 队列最大消息数量，0表示不限制
     */
    private long maxLength = 0;

    // ======================== 内部枚举 ========================

    /**
     * 交换机类型枚举
     */
    public enum ExchangeType {
        /**
         * 直连交换机，根据路由键精确匹配
         */
        DIRECT,
        /**
         * 扇出交换机，广播到所有绑定的队列
         */
        FANOUT,
        /**
         * 主题交换机，根据路由键模式匹配
         */
        TOPIC,
        /**
         * 头交换机，根据消息头匹配
         */
        HEADERS
    }

    // ======================== Builder ========================

    /**
     * 创建配置Builder
     *
     * @param queueName 队列名称
     * @return 配置Builder实例
     */
    public static Builder builder(String queueName) {
        return new Builder(queueName);
    }

    /**
     * MQ消费者配置Builder
     */
    public static class Builder {

        private final MqConsumerProperties props;

        private Builder(String queueName) {
            this.props = new MqConsumerProperties();
            this.props.setQueueName(queueName);
        }

        /**
         * 设置交换机名称
         *
         * @param exchangeName 交换机名称
         * @return 当前Builder实例
         */
        public Builder exchange(String exchangeName) {
            props.setExchangeName(exchangeName);
            return this;
        }

        /**
         * 设置路由键
         *
         * @param routingKey 路由键
         * @return 当前Builder实例
         */
        public Builder routingKey(String routingKey) {
            props.setRoutingKey(routingKey);
            return this;
        }

        /**
         * 设置交换机类型
         *
         * @param exchangeType 交换机类型
         * @return 当前Builder实例
         */
        public Builder exchangeType(ExchangeType exchangeType) {
            props.setExchangeType(exchangeType);
            return this;
        }

        /**
         * 设置ACK模式
         *
         * @param acknowledgeMode ACK模式
         * @return 当前Builder实例
         */
        public Builder acknowledgeMode(AcknowledgeMode acknowledgeMode) {
            props.setAcknowledgeMode(acknowledgeMode);
            return this;
        }

        /**
         * 设置消费者并发度
         *
         * @param concurrency 并发度字符串，格式 "min-max"
         * @return 当前Builder实例
         */
        public Builder concurrency(String concurrency) {
            props.setConcurrency(concurrency);
            return this;
        }

        /**
         * 设置预取数量
         *
         * @param prefetchCount 预取数量
         * @return 当前Builder实例
         */
        public Builder prefetchCount(int prefetchCount) {
            props.setPrefetchCount(prefetchCount);
            return this;
        }

        /**
         * 设置是否启用消费端批量拉取
         *
         * @param consumerBatchEnabled 是否启用
         * @return 当前Builder实例
         */
        public Builder consumerBatchEnabled(boolean consumerBatchEnabled) {
            props.setConsumerBatchEnabled(consumerBatchEnabled);
            return this;
        }

        /**
         * 设置单批最大消息数
         *
         * @param batchSize 批量大小
         * @return 当前Builder实例
         */
        public Builder batchSize(int batchSize) {
            props.setBatchSize(batchSize);
            return this;
        }

        /**
         * 设置是否启用重试
         *
         * @param retryEnabled 是否启用
         * @return 当前Builder实例
         */
        public Builder retryEnabled(boolean retryEnabled) {
            props.setRetryEnabled(retryEnabled);
            return this;
        }

        /**
         * 设置最大重试次数
         *
         * @param maxRetryCount 最大重试次数
         * @return 当前Builder实例
         */
        public Builder maxRetryCount(int maxRetryCount) {
            props.setMaxRetryCount(maxRetryCount);
            return this;
        }

        /**
         * 设置是否启用消费幂等
         *
         * @param idempotentEnabled 是否启用
         * @return 当前Builder实例
         */
        public Builder idempotentEnabled(boolean idempotentEnabled) {
            props.setIdempotentEnabled(idempotentEnabled);
            return this;
        }

        /**
         * 设置幂等键有效期
         *
         * @param idempotentTtl 幂等键有效期
         * @return 当前Builder实例
         */
        public Builder idempotentTtl(Duration idempotentTtl) {
            props.setIdempotentTtl(idempotentTtl);
            return this;
        }

        /**
         * 设置是否启用死信留痕
         *
         * @param deadLetterEnabled 是否启用
         * @return 当前Builder实例
         */
        public Builder deadLetterEnabled(boolean deadLetterEnabled) {
            props.setDeadLetterEnabled(deadLetterEnabled);
            return this;
        }

        /**
         * 设置业务性失败是否转入死信
         *
         * @param deadLetterOnBusinessError 是否转入死信
         * @return 当前Builder实例
         */
        public Builder deadLetterOnBusinessError(boolean deadLetterOnBusinessError) {
            props.setDeadLetterOnBusinessError(deadLetterOnBusinessError);
            return this;
        }

        /**
         * 设置死信队列名称
         *
         * @param deadLetterQueue 死信队列名称
         * @return 当前Builder实例
         */
        public Builder deadLetterQueue(String deadLetterQueue) {
            props.setDeadLetterQueue(deadLetterQueue);
            return this;
        }

        /**
         * 设置死信交换机
         *
         * @param deadLetterExchange 死信交换机名称
         * @return 当前Builder实例
         */
        public Builder deadLetterExchange(String deadLetterExchange) {
            props.setDeadLetterExchange(deadLetterExchange);
            return this;
        }

        /**
         * 设置死信路由键
         *
         * @param deadLetterRoutingKey 死信路由键
         * @return 当前Builder实例
         */
        public Builder deadLetterRoutingKey(String deadLetterRoutingKey) {
            props.setDeadLetterRoutingKey(deadLetterRoutingKey);
            return this;
        }

        /**
         * 设置消息TTL
         *
         * @param messageTtl 消息TTL（毫秒）
         * @return 当前Builder实例
         */
        public Builder messageTtl(long messageTtl) {
            props.setMessageTtl(messageTtl);
            return this;
        }

        /**
         * 设置队列最大消息数
         *
         * @param maxLength 最大消息数
         * @return 当前Builder实例
         */
        public Builder maxLength(long maxLength) {
            props.setMaxLength(maxLength);
            return this;
        }

        /**
         * 设置是否持久化队列
         *
         * @param durable 是否持久化
         * @return 当前Builder实例
         */
        public Builder durable(boolean durable) {
            props.setDurable(durable);
            return this;
        }

        /**
         * 设置是否自动声明
         *
         * @param autoDeclare 是否自动声明
         * @return 当前Builder实例
         */
        public Builder autoDeclare(boolean autoDeclare) {
            props.setAutoDeclare(autoDeclare);
            return this;
        }

        /**
         * 设置是否自动启动
         *
         * @param autoStart 是否自动启动
         * @return 当前Builder实例
         */
        public Builder autoStart(boolean autoStart) {
            props.setAutoStart(autoStart);
            return this;
        }

        /**
         * 构建配置实例
         *
         * @return MqConsumerProperties实例
         */
        public MqConsumerProperties build() {
            if (props.getQueueName() == null || props.getQueueName().isEmpty()) {
                AssertUtil.throwError(() ->
                    new IllegalArgumentException(I18nUtil.get("mq.consumer.queue.name.required")));
            }
            return props;
        }
    }

}
