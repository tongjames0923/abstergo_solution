package com.tbs.logging.rabbitmq;

import com.rabbitmq.client.Channel;
import com.tbs.rabbitmq.property.RabbitMqProperty;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Slf4j
public abstract class AbstractRabbitMqLogRecordWriter implements ChannelAwareMessageListener {

    @Resource
    protected RabbitMqProperty property;

    @Resource
    private AmqpTemplate amqpTemplate;

    @Resource
    private MessageConverter messageConverter;

    @Resource
    private ConnectionFactory connectionFactory;

    private SimpleMessageListenerContainer container;

    @PostConstruct
    public void init() {
        container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(property.getQueueName());
        container.setMessageListener(this);
        container.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        container.setConcurrency(property.getConsumerConcurrency());
        container.start();
        log.info("RabbitMQ消费者已启动，队列: {}，并发度: {}", property.getQueueName(),
            property.getConsumerConcurrency());
    }

    /**
     * 应用关闭时停止消息监听器容器
     */
    @PreDestroy
    public void destroy() {
        if (container != null && container.isRunning()) {
            container.stop();
            log.info("RabbitMQ消费者已停止，队列: {}", property.getQueueName());
        }
    }

    @Override
    public void onMessage(Message message, @Nullable Channel channel) throws Exception {

    }
}
