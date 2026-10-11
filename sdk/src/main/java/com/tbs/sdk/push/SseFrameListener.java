package com.tbs.sdk.push;

import com.tbs.sdk.model.FrameType;
import com.tbs.sdk.model.HelloInfo;
import com.tbs.sdk.model.PushFrame;

/**
 * SSE推送回调监听器
 * <p>
 * 全部方法均为默认空实现，调用方按需覆写。回调在SDK单线程执行器中分发且异常被隔离，
 * 不影响SSE流本身；实现方应保持回调轻量，耗时处理自行转交业务线程池。
 * 事件模型对齐C#端：状态变更、握手完成、信令、协议帧、异常五类回调。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface SseFrameListener {

    /**
     * 连接状态变更回调（建连/连接/重连/关闭）
     *
     * @param status 新状态
     */
    default void onStatusChanged(ConnectionStatus status) {
    }

    /**
     * hello握手完成回调（携带会话标识，此后可以上行帧）
     *
     * @param hello 握手信息
     */
    default void onHello(HelloInfo hello) {
    }

    /**
     * 收到SIGNAL信令回调
     * <p>
     * 信令本身无载荷，仅提示"该频道有新消息"；hello握手后亦触发一次兜底信令，
     * 以便宿主补齐离线期间消息与丢失的信令。宿主据此触发消息拉取。
     * </p>
     */
    default void onSignal() {
    }

    /**
     * 收到下行协议帧回调（data/pong等业务帧在此处理）
     *
     * @param type  帧类型
     * @param frame 协议帧
     */
    default void onFrame(FrameType type, PushFrame frame) {
    }

    /**
     * 连接异常回调（认证失败、建连失败或收到error帧，随后将自动换票重连）
     *
     * @param cause 异常
     */
    default void onError(Throwable cause) {
    }
}
