package com.tbs.sdk.push;

import java.util.List;
import java.util.Map;

/**
 * SSE推送订阅器契约
 * <p>
 * 编排推送组合通道全流程：认证签发一次性push-ticket → SSE建连 → hello握手 →
 * 上行SUBSCRIBE订阅 → 帧回调分发 → 断线重连（重新走auth流程，ticket一次性禁止复用）。
 * 实例持有连接与线程资源，用毕必须close。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ISsePushSubscriber {

    /**
     * 发起订阅（异步执行）
     * <p>
     * 内部依次完成认证、建连、握手与指定主题的SUBSCRIBE上行；
     * 连接结果经监听器回调通知，本方法不阻塞不抛出。
     * </p>
     *
     * @param topics     待订阅主题列表（可为空表示仅建立连接不订阅）
     * @param userId     用户ID（可选，注入会话属性）
     * @param attributes 会话属性（可选）
     * @param listener   帧回调监听器
     */
    void subscribe(List<String> topics, String userId, Map<String, String> attributes,
                   SseFrameListener listener);

    /**
     * 动态追加订阅主题
     *
     * @param topic 主题（非空且≤128字符）
     */
    void subscribeTopic(String topic);

    /**
     * 退订主题
     *
     * @param topic 主题
     */
    void unsubscribeTopic(String topic);

    /**
     * 上行心跳PING帧
     */
    void sendPing();

    /**
     * 上行自定义协议帧
     *
     * @param frame 协议帧（type必填，version自动补齐为当前协议版本）
     * @throws com.tbs.sdk.exception.SdkClientException 会话未建立或上行失败时抛出
     */
    void uploadFrame(com.tbs.sdk.model.PushFrame frame);

    /**
     * 获取当前会话标识
     *
     * @return 会话标识，未建立连接时为null
     */
    String currentSessionId();

    /**
     * 释放连接与线程资源（幂等，可重复调用）
     */
    void close();
}
