package com.tbs.rabbitmq.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;

import java.util.HashMap;
import java.util.Map;

/**
 * MQ资源声明工具类
 * <p>
 * 根据MqConsumerProperties配置自动声明RabbitMQ的队列、交换机和绑定关系。
 * 支持死信配置、TTL、队列最大长度等高级特性。
 * 开启死信留痕时自动声明扇出型死信交换机与死信队列（默认命名“队列名.dlx/队列名.dlq”），
 * 并将主队列的x-dead-letter-exchange指向该交换机。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Slf4j
public class MqResourceDeclarer {

    /**
     * 自动死信交换机名称后缀
     */
    public static final String DLX_SUFFIX = ".dlx";

    /**
     * 自动死信队列名称后缀
     */
    public static final String DLQ_SUFFIX = ".dlq";

    private MqResourceDeclarer() {
    }

    /**
     * 根据配置声明所有MQ资源（队列、交换机、绑定关系、死信资源）
     *
     * @param admin  AMQP管理器
     * @param props  消费者配置
     */
    public static void declare(AmqpAdmin admin, MqConsumerProperties props) {
        if (admin == null) {
            log.warn("AmqpAdmin未注入, 跳过自动声明, queue={}", props.getQueueName());
            return;
        }

        // 死信资源先于主队列声明，保证主队列的死信参数指向已存在的交换机
        if (shouldAutoDeclareDeadLetter(props)) {
            declareDeadLetterResources(admin, props);
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
                exchange = new FanoutExchange(exchangeName, props.isDurable(), props.isAutoDelete());
                break;
            case TOPIC:
                exchange = new TopicExchange(exchangeName, props.isDurable(), props.isAutoDelete());
                break;
            case HEADERS:
                exchange = new HeadersExchange(exchangeName, props.isDurable(), props.isAutoDelete());
                break;
            case DIRECT:
            default:
                exchange = new DirectExchange(exchangeName, props.isDurable(), props.isAutoDelete());
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

        String deadLetterExchange = resolveDeadLetterExchange(props);
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

    /**
     * 是否需要自动声明死信资源
     * <p>
     * 仅当开启死信留痕且未自行指定死信交换机时由框架托管声明；
     * 自行指定死信交换机的存量配置维持原有行为（由使用方自行管理死信资源）。
     * </p>
     *
     * @param props 消费者配置
     * @return true表示需要自动声明死信交换机与死信队列
     */
    public static boolean shouldAutoDeclareDeadLetter(MqConsumerProperties props) {
        return props.isDeadLetterEnabled() && props.getDeadLetterExchange() == null;
    }

    /**
     * 解析生效的死信交换机名称
     *
     * @param props 消费者配置
     * @return 死信交换机名称，未启用死信时返回null
     */
    public static String resolveDeadLetterExchange(MqConsumerProperties props) {
        if (props.getDeadLetterExchange() != null) {
            return props.getDeadLetterExchange();
        }
        if (props.isDeadLetterEnabled()) {
            return props.getQueueName() + DLX_SUFFIX;
        }
        return null;
    }

    /**
     * 解析生效的死信队列名称
     *
     * @param props 消费者配置
     * @return 死信队列名称，未启用死信时返回null
     */
    public static String resolveDeadLetterQueue(MqConsumerProperties props) {
        if (!props.isDeadLetterEnabled() && props.getDeadLetterQueue() == null) {
            return null;
        }
        return props.getDeadLetterQueue() != null ? props.getDeadLetterQueue()
            : props.getQueueName() + DLQ_SUFFIX;
    }

    /**
     * 声明死信交换机（扇出型）、死信队列及绑定
     *
     * @param admin AMQP管理器
     * @param props 消费者配置
     */
    private static void declareDeadLetterResources(AmqpAdmin admin, MqConsumerProperties props) {
        String dlxName = resolveDeadLetterExchange(props);
        String dlqName = resolveDeadLetterQueue(props);

        admin.declareQueue(new Queue(dlqName, true, false, false));
        admin.declareExchange(new FanoutExchange(dlxName, true, false));
        admin.declareBinding(new Binding(dlqName, Binding.DestinationType.QUEUE, dlxName, "", null));

        log.info("死信资源声明完成, queue={}, dlx={}, dlq={}", props.getQueueName(), dlxName, dlqName);
    }

}
