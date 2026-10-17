package com.tbs.mq.v2.consume;

import com.tbs.mq.v2.core.IMqContext;
import com.tbs.mq.v2.exception.MqConsumeException;

import java.io.Serializable;

public interface IMqConsumer<T extends Serializable> {
    /**
     * 消费者业务id
     *
     * @return
     */
    String businessId();

    /**
     * 消费者id
     *
     * @return
     */
    Integer consumeId();

    /**
     *
     * @param message
     * @param context
     */
    void consume(T message, IMqContext<T> context) throws MqConsumeException;

}
