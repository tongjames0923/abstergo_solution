package com.tbs.logging.rabbitmq;

import com.tbs.logging.form.LoggingRecordForm;
import com.tbs.rabbitmq.support.AbstractMqConsumer;
import lombok.extern.slf4j.Slf4j;

/**
 * RabbitMQ日志记录抽象类
 *
 * @author tongj
 */
@Slf4j
public abstract class AbstractRabbitMqLogRecordWriter extends AbstractMqConsumer<LoggingRecordForm> {
}
