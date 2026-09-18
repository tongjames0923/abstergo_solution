package com.tbs.push.api.session;

import com.tbs.push.model.ConnectionStateEnum;
import com.tbs.push.model.PushFrame;

/**
 * 统一连接会话契约
 * <p>
 * 所有传输适配层（WebSocket/SSE等）对一条物理连接的同构抽象，上层
 * （分发器/协调器/投递记录）只面向本接口，完全零传输感知：
 * <ul>
 *     <li>发送：写入协议帧，实现必须吞掉底层异常返回false（投递失败由记录层如实留痕）</li>
 *     <li>状态：CONNECTING → CONNECTED → CLOSED 的单点状态机</li>
 *     <li>活性：心跳监控依赖touch/lastActive判定空闲超时</li>
 *     <li>属性：供自定义投递协调器做目标解析的会话维度元数据</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IConnectionSession {

    /**
     * 获取会话唯一标识
     *
     * @return 会话唯一标识
     */
    String getSessionId();

    /**
     * 获取传输类型标识
     *
     * @return 传输类型标识（见PushFrameConstants传输常量）
     */
    String getTransportType();

    /**
     * 获取当前连接状态
     *
     * @return 当前连接状态
     */
    ConnectionStateEnum getState();

    /**
     * 标记会话握手完成进入可用态
     * <p>
     * 仅允许协议层在握手校验通过后调用；重复调用无副作用。
     * </p>
     */
    void markConnected();

    /**
     * 发送协议帧
     * <p>
     * 实现不得向调用方抛出底层异常：发送失败（连接断开/编解码失败等）返回false，
     * 由分发器据此记录投递结果。
     * </p>
     *
     * @param frame 协议帧
     * @return true表示帧已写入传输通道
     */
    boolean send(PushFrame frame);

    /**
     * 关闭会话
     * <p>
     * 幂等：已关闭的会话重复关闭无副作用。
     * </p>
     *
     * @param reason 关闭原因标识
     */
    void close(String reason);

    /**
     * 触碰会话活性时间戳
     * <p>
     * 传输层在收到/发出任何帧时调用，刷新空闲判定基准。
     * </p>
     */
    void touch();

    /**
     * 获取最近一次活性时间戳（毫秒）
     *
     * @return 最近一次活性时间戳
     */
    long getLastActiveTime();

    /**
     * 设置会话属性
     *
     * @param key   属性键
     * @param value 属性值
     */
    void setAttribute(String key, String value);

    /**
     * 获取会话属性
     *
     * @param key 属性键
     * @return 属性值，不存在时返回null
     */
    String getAttribute(String key);

}
