package com.tbs.rabbitmq.support;

import lombok.Data;
import org.springframework.amqp.core.AcknowledgeMode;

/**
 * MQ消费者配置属性
 * <p>
 * 集中管理AbstractMqConsumer的所有可配置维度，支持通过Builder模式按实例独立配置。 每个属性均有合理默认值，子类只需覆盖关心的配置项。
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
     * 是否在PostConstruct阶段自动启动消费者
     */
    private boolean autoStart = true;

    // ======================== 重试策略 ========================

    /**
     * 是否启用重试机制
     */
    private boolean retryEnabled = true;

    /**
     * 最大重试次数，超过后丢弃消息（或路由到死信队列）
     */
    private int maxRetryCount = 3;

    // ======================== 死信与TTL ========================

    /**
     * 死信交换机名称，为null则不配置死信
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
                throw new IllegalArgumentException("queueName must not be empty");
            }
            return props;
        }
    }

}
