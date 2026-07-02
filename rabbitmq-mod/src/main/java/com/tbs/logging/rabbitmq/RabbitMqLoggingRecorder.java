package com.tbs.logging.rabbitmq;

import com.tbs.common.util.base.StrUtil;
import com.tbs.logging.core.ILogRecorder;
import com.tbs.logging.form.LoggingRecordForm;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.annotation.Resource;

@Slf4j
public class RabbitMqLoggingRecorder implements ILogRecorder {

    @Resource
    RabbitTemplate rabbitTemplate;

    /**
     * 交换机名称
     */
    @Getter
    @Setter
    private String exchangeName;
    /**
     * 路由键
     */
    @Getter
    @Setter
    private String routingKey;

    public RabbitMqLoggingRecorder(String exchangeName, String routingKey) {
        if (StrUtil.isBlank(exchangeName)) {
            throw new IllegalArgumentException("exchangeName can not be null");
        }
        if (StrUtil.isBlank(routingKey)) {
            throw new IllegalArgumentException("routingKey can not be null");
        }
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;

    }

    @Override
    public void record(LoggingRecordForm recordForm) {
        try {
            rabbitTemplate.convertAndSend(getExchangeName(), getRoutingKey(), recordForm);
        } catch (Exception e) {
            log.error("RabbitMQ记录日志失败: {},日志内容:{}", e.getMessage(), recordForm);
            log.error("异常详情-------", e);
        }

    }
}
