package com.tbs.sdk.push;

import com.tbs.sdk.model.PushFrame;
import java.util.List;
import java.util.Map;

/**
 * SSE推送订阅器契约
 * <p>
 * 编排推送组合通道全流程：认证签发一次性push-ticket → SSE建连（携票作query与同源令牌头）→
 * 收hello帧取sessionId → 上行SUBSCRIBE订阅全部已登记主题 → 周期PING保活 → 断线换票指数退避
 * +随机抖动无限重连（一次性ticket被服务端原子消费，重连必须重新签票）。实例持有连接与线程资源，
 * 用毕必须close。主题与心跳/退避参数经 {@link com.tbs.sdk.config.PushOptions} 配置。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ISsePushSubscriber {

    /**
     * 发起订阅（异步建连，幂等：已有连接循环在运行则复用）
     * <p>
     * 内部依次完成认证、建连与握手；连接结果经监听器回调通知，本方法不阻塞。
     * topics为空时按per-user缺省频道（topicPrefix + userId）订阅。
     * </p>
     *
     * @param topics     待订阅主题列表（为空表示按userId派生缺省频道）
     * @param userId     用户ID（用于派生缺省主题并注入认证表单）
     * @param attributes 会话属性（可选）
     * @param listener   回调监听器
     * @throws com.tbs.sdk.exception.SdkClientException 监听器为空或无可用登录令牌时抛出
     */
    void subscribe(List<String> topics, String userId, Map<String, String> attributes, SseFrameListener listener);

    /**
     * 动态追加订阅主题
     * <p>
     * 连接就绪（CONNECTED）时立即上行SUBSCRIBE帧；未就绪时仅登记，待hello握手后随全量主题统一订阅。
     * 重复主题幂等忽略。
     * </p>
     *
     * @param topics 主题（非空且≤128字符）
     */
    void subscribeTopics(String... topics);

    /**
     * 动态移除订阅主题（连接就绪时同步上行UNSUBSCRIBE帧）
     *
     * @param topics 主题
     */
    void unsubscribeTopics(String... topics);

    /**
     * 上行心跳PING帧（连接未就绪时静默忽略）
     */
    void sendPing();

    /**
     * 上行自定义协议帧（同步阻塞，供高级场景使用）
     *
     * @param frame 协议帧（type必填，version缺省时自动补齐为当前协议版本）
     * @throws com.tbs.sdk.exception.SdkClientException 会话未建立或上行失败时抛出
     */
    void uploadFrame(PushFrame frame);

    /**
     * 获取当前连接状态
     *
     * @return 连接状态
     */
    ConnectionStatus currentStatus();

    /**
     * 获取当前会话标识（hello握手完成后可用）
     *
     * @return 会话标识，未握手时为null
     */
    String currentSessionId();

    /**
     * 主动关闭连接并停止重连（幂等，可重复调用；关闭后可再次subscribe复用实例）
     */
    void close();
}
