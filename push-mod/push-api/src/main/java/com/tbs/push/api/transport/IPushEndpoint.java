package com.tbs.push.api.transport;

import com.tbs.push.model.PushMessage;

import java.util.Map;

/**
 * 推送统一门面契约
 * <p>
 * 业务侧接入推送能力的唯一入口：业务消息先进入消息源，再由分发器按协调器
 * 解析的目标完成投递（投递一次即可），业务方完全不感知传输与订阅细节。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IPushEndpoint {

    /**
     * 发布业务消息（自动生成消息标识）
     *
     * @param topic   业务主题
     * @param payload 业务载荷对象，由编解码器序列化
     * @return 消息唯一标识
     */
    String publish(String topic, Object payload);

    /**
     * 发布业务消息（自动生成消息标识，附元数据）
     *
     * @param topic    业务主题
     * @param payload  业务载荷对象，由编解码器序列化
     * @param metadata 消息元数据，供自定义协调器做目标解析
     * @return 消息唯一标识
     */
    String publish(String topic, Object payload, Map<String, String> metadata);

    /**
     * 发布已构造好的业务消息
     *
     * @param message 业务消息
     * @return 消息唯一标识
     */
    String publish(PushMessage message);

    /**
     * 统计全部在线会话数量
     *
     * @return 在线会话数量
     */
    int onlineCount();

}
