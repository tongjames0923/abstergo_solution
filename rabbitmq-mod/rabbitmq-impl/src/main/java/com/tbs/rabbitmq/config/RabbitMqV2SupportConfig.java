package com.tbs.rabbitmq.config;

import com.tbs.mq.v2.core.IMqMessagePayloadConverter;
import com.tbs.mq.v2.core.MqPayloadConverterRegistry;
import com.tbs.mq.v2.impl.JdkMqPayloadConverter;
import com.tbs.mq.v2.producer.IMqMessageProducer;
import com.tbs.rabbitmq.v2.RabbitMqMessageProducer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * RabbitMQ v2 生产段组件装配
 * <p>
 * 为 v2 记录驱动模型的生产/序列化链路提供厂商侧默认实现，全部允许被使用方覆盖：
 * <ul>
 *     <li>{@link JdkMqPayloadConverter} — 默认负载转换器（格式 jdk）；</li>
 *     <li>{@link MqPayloadConverterRegistry} — 按 payloadFormat 路由收发两端共用；</li>
 *     <li>{@link RabbitMqMessageProducer} — 信封到厂商消息的 v2 发送实现。</li>
 * </ul>
 * 与 v1 的 {@code RabbitMqSupportConfig}（统一 IMqProducer / 重投递）分置，互不影响。
 * </p>
 *
 * @author tongj
 * @since 2.0.0
 */
@Configuration
public class RabbitMqV2SupportConfig {

    /**
     * 默认 JDK 序列化负载转换器
     *
     * @return 格式为 jdk 的负载转换器
     */
    @Bean
    @ConditionalOnMissingBean(IMqMessagePayloadConverter.class)
    public IMqMessagePayloadConverter jdkMqPayloadConverter() {
        return new JdkMqPayloadConverter();
    }

    /**
     * 负载转换器注册表：汇集容器内全部转换器，按 payloadFormat 路由
     *
     * @param converters 容器内的全部负载转换器
     * @return 转换器注册表
     */
    @Bean
    @ConditionalOnMissingBean(MqPayloadConverterRegistry.class)
    public MqPayloadConverterRegistry mqPayloadConverterRegistry(List<IMqMessagePayloadConverter> converters) {
        return new MqPayloadConverterRegistry(converters);
    }

    /**
     * v2 消息发送实现
     *
     * @param rabbitTemplate    RabbitMQ 发送模板
     * @param converterRegistry 负载转换器注册表
     * @return v2 消息发送器
     */
    @Bean
    @ConditionalOnMissingBean(IMqMessageProducer.class)
    public IMqMessageProducer mqMessageProducer(RabbitTemplate rabbitTemplate,
        MqPayloadConverterRegistry converterRegistry) {
        return new RabbitMqMessageProducer(rabbitTemplate, converterRegistry);
    }
}
