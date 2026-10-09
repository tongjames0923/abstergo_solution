package com.tbs.push.impl.session;

import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.api.session.ISessionRegistry;
import com.tbs.push.api.transport.ITransportAdapter;
import com.tbs.push.constants.PushFrameConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 心跳空闲超时监控器
 * <p>
 * 协议层心跳机制的统一执行点（跨传输一致）：周期性扫描全部注册会话， 最近活性时间戳距今超过空闲超时的会话判定为死连接，关闭并注销。 超时时间配置为非正数时本监控器不做任何动作（心跳监控关闭）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class HeartbeatMonitor {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatMonitor.class);

    /**
     * 检查周期下限（毫秒），避免超时时间过短导致检查过频
     */
    private static final long MIN_CHECK_INTERVAL_MILLIS = 1_000L;

    /**
     * 会话注册表
     */
    private final ISessionRegistry sessionRegistry;

    /**
     * 空闲超时（毫秒），非正数表示关闭心跳监控
     */
    private final long heartbeatTimeoutMillis;

    /**
     * 传输适配器延迟获取（存在时主动关闭走完整清理链，缺失时退化为基线动作）
     */
    private final ObjectProvider<ITransportAdapter> transportProvider;

    /**
     * 检查线程
     */
    private ScheduledExecutorService scheduler;

    /**
     * 构造心跳监控器
     *
     * @param sessionRegistry        会话注册表
     * @param heartbeatTimeoutMillis 空闲超时（毫秒），非正数表示关闭监控
     * @param transportProvider      传输适配器提供者（超时会话经其主动关闭，清理链完整闭环）
     */
    public HeartbeatMonitor(ISessionRegistry sessionRegistry, long heartbeatTimeoutMillis,
        ObjectProvider<ITransportAdapter> transportProvider) {
        this.sessionRegistry = sessionRegistry;
        this.heartbeatTimeoutMillis = heartbeatTimeoutMillis;
        this.transportProvider = transportProvider;
    }

    /**
     * 启动监控
     */
    @PostConstruct
    public void start() {
        if (heartbeatTimeoutMillis <= 0) {
            log.info("心跳空闲超时未启用(heartbeatTimeoutMillis={}), 跳过心跳监控", heartbeatTimeoutMillis);
            return;
        }
        long checkInterval = Math.max(MIN_CHECK_INTERVAL_MILLIS, heartbeatTimeoutMillis / 3);
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "push-heartbeat-monitor");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(this::checkIdleSessions, checkInterval, checkInterval, TimeUnit.MILLISECONDS);
        log.info("心跳空闲监控已启动, 超时={}ms, 检查间隔={}ms", heartbeatTimeoutMillis, checkInterval);
    }

    /**
     * 停止监控
     */
    @PreDestroy
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    /**
     * 扫描并关闭空闲超时会话（异常安全，保证调度线程存活）
     * <p>
     * 优先经传输适配器主动关闭：物理拆除+清理链（注销/订阅清理/断开拦截器）
     * 一次闭环——超时的半开连接事件回传不可靠，不能依赖其触发清理；
     * 适配器缺失（传输未装配）时退化为直接关闭与基线注销。
     * </p>
     */
    private void checkIdleSessions() {
        try {
            long now = System.currentTimeMillis();
            ITransportAdapter adapter = transportProvider.getIfAvailable();
            for (IConnectionSession session : sessionRegistry.all()) {
                if (now - session.getLastActiveTime() <= heartbeatTimeoutMillis) {
                    continue;
                }
                log.warn("会话心跳空闲超时, sessionId:{}, transport:{}", session.getSessionId(),
                    session.getTransportType());
                if (adapter != null) {
                    adapter.closeSession(session.getSessionId(), PushFrameConstants.CLOSE_REASON_HEARTBEAT_TIMEOUT);
                } else {
                    session.close(PushFrameConstants.CLOSE_REASON_HEARTBEAT_TIMEOUT);
                    sessionRegistry.unregister(session.getSessionId());
                }
            }
        } catch (Throwable error) {
            log.error("心跳空闲检查异常", error);
        }
    }

}
