package com.tbs.push.impl.protocol;

import com.tbs.push.api.protocol.IHeartbeatRecommender;
import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.model.protocol.HeartbeatAdvice;

/**
 * 默认心跳推荐器：按服务端心跳空闲超时推导
 * <p>
 * 推荐周期 = {@code min(heartbeatTimeoutMillis / INTERVAL_DIVISOR, KEEPALIVE_MAX_INTERVAL_MILLIS)}：
 * 除以 divisor 使周期显著小于服务端回收点，再对一个保守上限封顶——上限的存在是因为服务端无法感知
 * 中间层（反向代理/网关）的空闲读超时，纯按超时推导可能给出过大的周期而让下游流被中间层掐断。
 * 最迟期限 = {@code heartbeatTimeoutMillis * DEADLINE_NUMERATOR / DEADLINE_DENOMINATOR}，
 * 严格早于回收点，给客户端留出主动重连的自保窗口。
 * 心跳超时配置为非正数（关闭监控）时返回 {@code null}，不下发推荐。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class TimeoutDeriveHeartbeatRecommender implements IHeartbeatRecommender {

    /**
     * 推荐周期相对空闲超时的分母
     */
    private static final int INTERVAL_DIVISOR = 4;

    /**
     * 推荐周期封顶上限（毫秒）：兜住服务端不可见的中间层空闲读超时
     */
    private static final long KEEPALIVE_MAX_INTERVAL_MILLIS = 30_000L;

    /**
     * 最迟期限分子
     */
    private static final int DEADLINE_NUMERATOR = 3;

    /**
     * 最迟期限分母
     */
    private static final int DEADLINE_DENOMINATOR = 4;

    /**
     * 服务端心跳空闲超时（毫秒），取自 PushProperty
     */
    private final long heartbeatTimeoutMillis;

    /**
     * 构造默认心跳推荐器
     *
     * @param heartbeatTimeoutMillis 服务端心跳空闲超时（毫秒），非正数表示关闭监控
     */
    public TimeoutDeriveHeartbeatRecommender(long heartbeatTimeoutMillis) {
        this.heartbeatTimeoutMillis = heartbeatTimeoutMillis;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HeartbeatAdvice recommend(IConnectionSession session) {
        if (heartbeatTimeoutMillis <= 0) {
            return null;
        }
        long interval = Math.min(heartbeatTimeoutMillis / INTERVAL_DIVISOR, KEEPALIVE_MAX_INTERVAL_MILLIS);
        long deadline = heartbeatTimeoutMillis * DEADLINE_NUMERATOR / DEADLINE_DENOMINATOR;
        return new HeartbeatAdvice(interval, deadline);
    }

}
