package com.tbs.rabbitmq.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

import java.util.HashMap;
import java.util.Map;

/**
 * MQ资源声明工具类
 * <p>
 * 根据MqConsumerProperties配置自动声明RabbitMQ的队列、交换机和绑定关系。
 * 支持死信配置、TTL、队列最大长度等高级特性。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Slf4j
public class MqResourceDeclarer {

    private MqResourceDeclarer() {
    }

    /**
     * 根据配置声明所有MQ资源（队列、交换机、绑定关系）
     *
     * @param admin  AMQP管理器
     * @param props  消费者配置
     */
    public static void declare(AmqpAdmin admin, MqConsumerProperties props) {
        if (admin == null) {
            log.warn("AmqpAdmin未注入, 跳过自动声明, queue={}", props.getQueueName());
            return;
        }

        declareQueue(admin, props);

        String exchangeName = props.getExchangeName();
        if (exchangeName != null) {
            declareExchange(admin, props, exchangeName);

            String routingKey = props.getRoutingKey();
            if (routingKey != null) {
                declareBinding(admin, props.getQueueName(), exchangeName, routingKey);
            }
        }

        log.info("RabbitMQ资源自动声明完成, queue={}, exchange={}, routingKey={}",
            props.getQueueName(), props.getExchangeName(), props.getRoutingKey());
    }

    /**
     * 声明队列
     *
     * @param admin AMQP管理器
     * @param props 消费者配置
     */
    private static void declareQueue(AmqpAdmin admin, MqConsumerProperties props) {
        Map<String, Object> args = buildQueueArguments(props);
        Queue queue = new Queue(
            props.getQueueName(),
            props.isDurable(),
            props.isExclusive(),
            props.isAutoDelete(),
            args
        );
        admin.declareQueue(queue);
    }

    /**
     * 声明交换机
     *
     * @param admin        AMQP管理器
     * @param props        消费者配置
     * @param exchangeName 交换机名称
     */
    private static void declareExchange(AmqpAdmin admin, MqConsumerProperties props, String exchangeName) {
        org.springframework.amqp.core.Exchange exchange;
        switch (props.getExchangeType()) {
            case FANOUT:
                exchange = new FanoutExchange(exchangeName, true, false);
                break;
            case TOPIC:
                exchange = new TopicExchange(exchangeName, true, false);
                break;
            case HEADERS:
                exchange = new HeadersExchange(exchangeName, true, false);
                break;
            case DIRECT:
            default:
                exchange = new DirectExchange(exchangeName, true, false);
                break;
        }
        admin.declareExchange(exchange);
    }

    /**
     * 声明绑定关系
     *
     * @param admin        AMQP管理器
     * @param queueName    队列名称
     * @param exchangeName 交换机名称
     * @param routingKey   路由键
     */
    private static void declareBinding(AmqpAdmin admin, String queueName, String exchangeName, String routingKey) {
        Binding binding = new Binding(
            queueName,
            Binding.DestinationType.QUEUE,
            exchangeName,
            routingKey,
            null
        );
        admin.declareBinding(binding);
    }

    /**
     * 构建队列扩展参数（死信、TTL、最大长度等）
     *
     * @param props 消费者配置
     * @return 队列参数映射
     */
    private static Map<String, Object> buildQueueArguments(MqConsumerProperties props) {
        Map<String, Object> args = new HashMap<>();

        String deadLetterExchange = props.getDeadLetterExchange();
        if (deadLetterExchange != null) {
            args.put("x-dead-letter-exchange", deadLetterExchange);
            String deadLetterRoutingKey = props.getDeadLetterRoutingKey();
            if (deadLetterRoutingKey != null) {
                args.put("x-dead-letter-routing-key", deadLetterRoutingKey);
            }
        }

        long ttl = props.getMessageTtl();
        if (ttl > 0) {
            args.put("x-message-ttl", ttl);
        }

        long maxLength = props.getMaxLength();
        if (maxLength > 0) {
            args.put("x-max-length", maxLength);
        }

        return args;
    }

}
