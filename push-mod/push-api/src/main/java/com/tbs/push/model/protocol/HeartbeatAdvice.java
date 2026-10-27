package com.tbs.push.model.protocol;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 心跳推荐值
 * <p>
 * 由服务端 {@link com.tbs.push.api.protocol.IHeartbeatRecommender} 生成、随 HELLO 握手帧下发，
 * 客户端据此发起周期心跳（心跳方向仍为客户端主动上行 PING，推荐值只决定节奏与自保期限）：
 * <ul>
 *     <li>intervalMillis 推荐心跳周期：客户端按此间隔上行 PING</li>
 *     <li>deadlineMillis 最迟心跳期限：距上次心跳超过此值即视为链路可能已陈旧，客户端应主动重连
 *     （应对定时器被宿主节流拉伸、或半开连接客户端未察觉的场景），严格早于服务端空闲回收点</li>
 * </ul>
 * 二者均为相对毫秒，不用绝对时间戳，以规避前后端时钟偏差。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Getter
@RequiredArgsConstructor
public class HeartbeatAdvice {

    /**
     * 推荐心跳周期（毫秒）
     */
    private final long intervalMillis;

    /**
     * 最迟心跳期限（毫秒）
     */
    private final long deadlineMillis;

}
