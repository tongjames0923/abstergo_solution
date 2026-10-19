package com.tbs.app.config;

import com.tbs.sql.datasource.interceptor.JdbcExecutionInterceptor;
import com.tbs.sql.datasource.model.InterceptVerdict;
import com.tbs.sql.datasource.model.JdbcConnectionContext;
import com.tbs.sql.datasource.model.JdbcExecuteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数据库连接准入限流器（演示用业务侧拦截器）——职责已收窄为「只做一个 Hikari 给不了的闸门：排队深度上限 + 秒拒」。
 * <p>
 * 定位：连接数上限交给 Hikari {@code maximum-pool-size}、获取连接的等待超时交给 Hikari {@code connection-timeout}。
 * 本类<b>不再</b>自己数许可、也不再自己 {@code tryAcquire(timeout)}（那两点与 Hikari 重复，且易踩
 * {@code permits>pool} 倒挂）。它只保留独有的价值：限制「同时在途（已放行、尚未归还）的连接请求数」，
 * 一旦超过 {@code maxWaiters} 就<b>立即拒绝</b>（0 等待），避免在 Hikari 前堆出无法被服务的深队列。
 * </p>
 * <p>
 * 工作机制：
 * <ol>
 *   <li>{@link #interceptConnection}：把「在途计数」+1；若加完超过 {@code maxWaiters}，则回退并<b>秒拒</b>
 *       （计 {@code deniedQueue}），否则放行——真正的连接分配与等待交给后面的 Hikari；</li>
 *   <li>{@link #onConnectionRelease}（Connection.close 或借出失败时由框架回调）：在途计数 -1，与放行配对。</li>
 * </ol>
 * 因此 {@code maxWaiters} ≈ Hikari 正在服务的连接(≈pool-size) + 允许排在 Hikari 获取队列里的总量，
 * 建议取 {@code pool-size} 加一定余量（如 pool=20 → maxWaiters=100）。
 * </p>
 * <p>
 * ponytail: 在途计数在放行时 +1、在归还时 -1；若业务借连接后不 close（未用 try-with-resources）会导致计数虚高、
 * 提前触发秒拒，依赖调用方归还纪律。阈值软上限（并发下有极小概率瞬时超出 1~2，作为 shed 足够，不做精确配额）。
 * 累计拒绝计数进程级单调递增，压测接口读取"运行前后差值"。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class ConnectionAdmissionLimiter implements JdbcExecutionInterceptor {

    /** 连接准入限流专用日志器，用独立名称便于单独配置日志级别。 */
    private static final Logger CONN_LIMIT_LOG = LoggerFactory.getLogger("datasource.connection.limiter");

    /** 同时在途（已放行、尚未归还）的连接请求数上限，超过则快速拒绝（负载 shedding）。 */
    private final int maxWaiters;

    /** 当前在途（已放行未归还）的连接请求数。放行 +1，归还/借出失败 -1。 */
    private final AtomicInteger inflight = new AtomicInteger(0);

    /** 因在途数超限被快速拒绝的累计次数。 */
    private final AtomicLong deniedQueue = new AtomicLong();

    public ConnectionAdmissionLimiter(int maxWaiters) {
        this.maxWaiters = maxWaiters;
    }

    @Override
    public InterceptVerdict interceptConnection(JdbcConnectionContext context) {
        int cur = inflight.incrementAndGet();
        if (cur > maxWaiters) {
            // 回退本次占用并秒拒：不进入 Hikari 队列，避免堆出无法被服务的深队列
            inflight.decrementAndGet();
            deniedQueue.incrementAndGet();
            CONN_LIMIT_LOG.warn("[准入] 拒绝(在途超限{}): dsId={}, thread={}", maxWaiters,
                    context.getDataSourceId(), context.getThreadName());
            return InterceptVerdict.deny("datasource.intercept.connection.queue.overload");
        }
        // 放行：连接分配与等待交给后面的 Hikari；在途计数留待 onConnectionRelease 归还
        CONN_LIMIT_LOG.debug("[准入] 放行: dsId={}, thread={}, 在途={}/{}",
                context.getDataSourceId(), context.getThreadName(), cur, maxWaiters);
        return InterceptVerdict.allow();
    }

    @Override
    public void onConnectionRelease(JdbcConnectionContext context) {
        int cur = inflight.decrementAndGet();
        CONN_LIMIT_LOG.debug("[归还] 在途-1: dsId={}, thread={}, 在途={}/{}",
                context.getDataSourceId(), context.getThreadName(), cur, maxWaiters);
    }

    @Override
    public InterceptVerdict interceptExecution(JdbcExecuteContext context) {
        return InterceptVerdict.allow();
    }

    /** 因在途数超限被拒绝的累计次数（进程级单调递增）。 */
    public long getDeniedQueueTotal() {
        return deniedQueue.get();
    }

    /** 当前在途（已放行未归还）的连接请求数，便于观测队列水位。 */
    public int getInflight() {
        return inflight.get();
    }

    /** 配置的在途上限，便于压测接口回显。 */
    public int getMaxWaiters() {
        return maxWaiters;
    }
}
