package com.tbs.mq.v2.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MqMessageVO<R, T extends Serializable> implements Serializable {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 业务id
     */
    private String businessId;

    /**
     * 原始消息
     */
    private R rawMessage;

    /**
     * 消息体
     */
    private T payload;


    /**
     * 消息标志
     *
     * @see com.tbs.mq.v2.enums.MqMessageStatusEnum
     */
    private Integer flag;

}
