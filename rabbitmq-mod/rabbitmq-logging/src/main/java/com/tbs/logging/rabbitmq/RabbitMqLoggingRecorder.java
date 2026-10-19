package com.tbs.logging.rabbitmq;

import com.tbs.common.util.base.StrUtil;
import com.tbs.logging.core.ILogRecorder;
import com.tbs.logging.form.LoggingRecordForm;
import com.tbs.mq.v2.producer.IMqMessageProducer;
import com.tbs.mq.v2.vo.MqMessageOutcomeVO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;

/**
 * RabbitMQ 日志记录器（投递端）
 * <p>
 * 投递已改走 v2 统一发送契约 {@link IMqMessageProducer}：信封以 {@code topic}=交换机、{@code routeKey}=路由键
 * 描述目标，负载由 v2 负载转换器序列化并携带 payloadType/payloadFormat/contentType 头。
 * 与接收段对称：v2 链路走 payloadType 头精确反序列化；v1 旧链路（厂商 {@code SerializerMessageConverter}）
 * 凭 contentType 兜底兼容，两代消费者均可读。
 * </p>
 */
@Slf4j
public class RabbitMqLoggingRecorder implements ILogRecorder {

    /**
     * v2 消息发送器，信封到厂商消息的组装与投递均由其承担
     */
    @Resource
    IMqMessageProducer mqMessageProducer;

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
            sendEnvelope(recordForm);
        } catch (Exception e) {
            log.error("RabbitMQ记录日志失败: {},日志内容:{}", e.getMessage(), recordForm);
            log.error("异常详情-------", e);
        }

    }

    @Override
    public void flush() {
        LoggingRecordForm recordForm = new LoggingRecordForm();
        try {
            recordForm.setFlushFlag(1);
            sendEnvelope(recordForm);
        } catch (Exception e) {
            log.error("RabbitMQ记录日志失败: {},日志内容:{}", e.getMessage(), recordForm);
            log.error("异常详情-------", e);
        }
    }

    /**
     * 组装 v2 信封并投递：交换机/路由键取自本记录器配置，负载为日志表单
     *
     * @param recordForm 待投递的日志表单（含刷新标志空表单）
     */
    private void sendEnvelope(LoggingRecordForm recordForm) {
        MqMessageOutcomeVO<LoggingRecordForm> envelope = MqMessageOutcomeVO.<LoggingRecordForm>builder()
            .topic(exchangeName).routeKey(routingKey).payload(recordForm).build();
        mqMessageProducer.send(envelope);
    }
}
