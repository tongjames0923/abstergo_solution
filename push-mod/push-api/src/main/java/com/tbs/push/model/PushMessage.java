package com.tbs.push.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 推送业务消息
 * <p>
 * 业务侧投递到消息源的完整消息快照，是分发编排的处理单元：
 * 业务来源消息先经门面{@code IPushEndpoint}进入消息源（{@code IPushMessageStore}），
 * 再由分发器按协调器解析出的目标投递，投递一次即可。
 * 载荷为已序列化文本，由业务方决定内容结构，协议层不做二次解释。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一标识，同时作为DATA帧的messageId与投递记录的关联键
     */
    private String id;

    /**
     * 业务主题，投递目标的默认匹配依据
     */
    private String topic;

    /**
     * 序列化后的业务载荷文本
     */
    private String payload;

    /**
     * 消息元数据快照，供自定义投递协调器做目标解析（如指定会话属性、追踪ID等）
     */
    private Map<String, String> metadata;

    /**
     * 入库时间
     */
    private Date enqueueTime;

}
