package com.tbs.cdc.service.property;

import com.tbs.rabbitmq.support.MqConsumerProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CDC MQ发出配置
 * <p>
 * 收拢CDC变更事件发往RabbitMQ的全部配置项，前缀{@code tbs.cdc.mq}。
 * 不加{@code @Component}，由{@code CdcServiceConfig}经{@code @EnableConfigurationProperties}激活。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "tbs.cdc.mq")
public class CdcMqProperty {

    /**
     * 总开关：false时不装配MQ发出监听器，事件仅进程内分发
     */
    private boolean enable = true;

    /**
     * 目标交换机名称
     */
    private String exchange = "tbs.cdc";

    /**
     * 交换机类型：direct / topic / fanout / headers（复用rabbitmq模块的ExchangeType枚举，yml配置不区分大小写）
     */
    private MqConsumerProperties.ExchangeType exchangeType = MqConsumerProperties.ExchangeType.TOPIC;

    /**
     * 交换机与自动声明队列是否持久化
     */
    private boolean durable = true;

    /**
     * 路由键模板，占位符{database}/{table}/{changeType}按事件渲染
     */
    private String routingPattern = "cdc.{database}.{table}.{changeType}";

}
