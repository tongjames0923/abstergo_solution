package com.tbs.cdc.service.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

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
     * 交换机类型：direct / topic / fanout / headers
     */
    private String exchangeType = "topic";

    /**
     * 交换机与自动声明队列是否持久化
     */
    private boolean durable = true;

    /**
     * 路由键模板，占位符{database}/{table}/{changeType}按事件渲染
     */
    private String routingPattern = "cdc.{database}.{table}.{changeType}";

    /**
     * 启动时是否自动声明目标交换机（解决发送侧只发不建导致的404）
     */
    private boolean autoDeclare = true;

    /**
     * 可选的队列绑定声明列表：随启动自动声明队列并绑定到目标交换机，为空则只声明交换机
     */
    private List<BindingItem> bindings = new ArrayList<>();

    /**
     * 队列绑定声明项
     *
     * @author abstergo
     * @since 1.0.0
     */
    @Data
    public static class BindingItem {

        /**
         * 队列名称
         */
        private String queue;

        /**
         * 绑定的路由键模式（topic交换机支持*与#通配）
         */
        private String routingKey = "#";
    }
}
