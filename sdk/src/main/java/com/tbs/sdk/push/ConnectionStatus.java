package com.tbs.sdk.push;

/**
 * SSE推送连接状态
 * <p>
 * 对齐前端与C#端的连接状态机，经 {@link SseFrameListener#onStatusChanged} 回调宿主：
 * 建连后依次经历 CONNECTING → CONNECTED（hello握手完成），断线退避重连期间为 RECONNECTING，
 * 主动close后为CLOSED且不再重连。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum ConnectionStatus {

    /**
     * 空闲（未启动）
     */
    IDLE,

    /**
     * 建连中
     */
    CONNECTING,

    /**
     * 已连接（hello握手完成）
     */
    CONNECTED,

    /**
     * 重连中（退避等待或换票建连）
     */
    RECONNECTING,

    /**
     * 已关闭（主动close，不再重连）
     */
    CLOSED
}
