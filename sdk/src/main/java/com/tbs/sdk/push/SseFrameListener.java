package com.tbs.sdk.push;

import com.tbs.sdk.model.FrameType;
import com.tbs.sdk.model.PushFrame;

/**
 * SSE信令帧回调监听器
 * <p>
 * 全部方法均为默认空实现，调用方按需覆写。回调在SDK独立的单线程执行器中
 * 分发且回调异常被隔离，不影响SSE流本身；实现方应保持回调轻量，
 * 耗时处理自行转交业务线程池。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface SseFrameListener {

    /**
     * 连接建立并握手成功（收到hello帧）后回调
     *
     * @param sessionId 会话标识
     */
    default void onConnected(String sessionId) {
    }

    /**
     * 收到协议帧回调（按帧类型分发，DATA/SIGNAL等业务帧在此处理）
     *
     * @param type  帧类型
     * @param frame 协议帧
     */
    default void onFrame(FrameType type, PushFrame frame) {
    }

    /**
     * 连接关闭回调（主动close、服务端关闭或重连耗尽后触发）
     *
     * @param reason 关闭原因（服务端协议原因串或SDK内部描述）
     */
    default void onClosed(String reason) {
    }

    /**
     * 连接异常回调（认证失败、建连失败或重连耗尽时触发）
     *
     * @param cause 异常
     */
    default void onError(Throwable cause) {
    }
}
