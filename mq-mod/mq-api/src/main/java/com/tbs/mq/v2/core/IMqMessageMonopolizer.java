package com.tbs.mq.v2.core;

import com.tbs.mq.v2.vo.MqMessageVO;

import java.io.Serializable;

/**
 * 消息抢占者
 *
 */
public interface IMqMessageMonopolizer {
    /**
     * 抢占消息
     *
     * @param message
     * @return
     */
    <T extends Serializable> boolean monopolize(MqMessageVO<T> message);

    /**
     * 释放消息
     *
     * @param message
     */
    <T extends Serializable> MqMessageVO<T> release(MqMessageVO<T> message);

}
