package com.tbs.rabbitmq.config;

import com.tbs.common.i18n.AbstractMessageSourceProvider;

import org.springframework.stereotype.Component;

/**
 * RabbitMQ模块消息源提供者
 * <p>
 * 自动注册MQ契约层的消息资源文件：i18n/messages-mq.properties。
 * 资源文件位于mq-api模块，随契约代码一同分发。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Component
public class RabbitMqMessageSourceProvider extends AbstractMessageSourceProvider {

    @Override
    public String getBasename() {
        return "i18n/messages-mq";
    }

    @Override
    public int getOrder() {
        // RabbitMQ模块优先级
        return -500;
    }
}
