package com.tbs.push.impl.protocol;

import com.tbs.push.api.protocol.IHeartbeatRecommender;
import com.tbs.push.api.protocol.IServerBusyIndicator;
import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.model.protocol.HeartbeatAdvice;

/**
 * 默认心跳推荐器
 * <p>
 * 两个下发值各有独立依据，不做一刀切的启发式：
 * <ul>
 *     <li>最迟期限 = 服务端心跳空闲回收点 {@code heartbeatTimeoutMillis}。心跳方向为客户端主动，
 *     该值即"服务端空闲多久后判死回收"，客户端据此在回收前自保重连，严格跟随真实回收时刻而非固定比例</li>
 *     <li>推荐周期 = 按 {@link IServerBusyIndicator} 报告的繁忙度在 [{@code floor}, {@code ceiling}] 内线性自适应：
 *     {@code ceiling = heartbeatTimeoutMillis / INTERVAL_CEILING_DIVISOR}（保证显著小于回收点），
 *     {@code floor = min(KEEPALIVE_WARM_INTERVAL_MILLIS, ceiling)}（空闲时下发，兜住服务端不可见的中间层
 *     空闲读超时使下游流保温）。<b>越忙周期越长</b>（抑制海量终端上行心跳风暴），<b>越闲周期越短</b>
 *     （更实时且下游更保温）</li>
 * </ul>
 * 心跳超时配置为非正数（关闭监控）时返回 {@code null}，不下发推荐。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DefaultHeartbeatRecommender implements IHeartbeatRecommender {

    /**
     * 空闲时的保温周期下限（毫秒）：兜住服务端不可见的中间层空闲读超时
     */
    private static final long KEEPALIVE_WARM_INTERVAL_MILLIS = 30_000L;

    /**
     * 繁忙时的周期上限相对回收点的分母：保证推荐周期显著小于回收点
     */
    private static final int INTERVAL_CEILING_DIVISOR = 4;

    /**
     * 服务端心跳空闲回收点（毫秒），同时作为下发给客户端的最迟期限
     */
    private final long heartbeatTimeoutMillis;

    /**
     * 繁忙度信号源
     */
    private final IServerBusyIndicator busyIndicator;

    /**
     * 构造默认心跳推荐器
     *
     * @param heartbeatTimeoutMillis 服务端心跳空闲回收点（毫秒），非正数表示关闭监控
     * @param busyIndicator          繁忙度信号源，不能为空
     */
    public DefaultHeartbeatRecommender(long heartbeatTimeoutMillis, IServerBusyIndicator busyIndicator) {
        this.heartbeatTimeoutMillis = heartbeatTimeoutMillis;
        this.busyIndicator = busyIndicator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HeartbeatAdvice recommend(IConnectionSession session) {
        if (heartbeatTimeoutMillis <= 0) {
            return null;
        }
        long deadline = heartbeatTimeoutMillis;
        long ceiling = deadline / INTERVAL_CEILING_DIVISOR;
        long floor = Math.min(KEEPALIVE_WARM_INTERVAL_MILLIS, ceiling);
        long interval = floor + Math.round((ceiling - floor) * clampBusy(busyIndicator.busyScore()));
        return new HeartbeatAdvice(interval, deadline);
    }

    /**
     * 夹取繁忙度到 [0,1]，容错信号源越界返回
     *
     * @param busy 原始繁忙度
     * @return [0,1] 区间内的繁忙度
     */
    private static double clampBusy(double busy) {
        if (busy < 0D) {
            return 0D;
        }
        return Math.min(1.0D, busy);
    }

}
