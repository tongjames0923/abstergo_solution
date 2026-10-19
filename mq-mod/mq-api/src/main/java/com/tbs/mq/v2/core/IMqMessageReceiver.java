package com.tbs.mq.v2.core;

import com.tbs.common.function.SConsumer;
import com.tbs.mq.v2.vo.MqMessageVO;

import java.io.Serializable;
import java.util.List;

/**
 * 消息接收者接口
 *
 * @param <R>
 * @param <T>
 */
public interface IMqMessageReceiver<R, T extends Serializable> {

    /**
     * 接收消息
     *
     * @param messages
     * @param onInqueueSuccess
     * @param onInqueueFail
     * @return
     */
    List<MqMessageVO<R, T>> receive(List<R> messages, SConsumer<R> onInqueueSuccess, SConsumer<R> onInqueueFail);

}
