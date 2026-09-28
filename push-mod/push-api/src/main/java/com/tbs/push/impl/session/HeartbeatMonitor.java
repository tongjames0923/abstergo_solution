package com.tbs.push.impl.session;

import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.api.session.ISessionRegistry;
import com.tbs.push.constants.PushFrameConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
     * 检查线程
     */
    private ScheduledExecutorService scheduler;

    /**
     * 构造心跳监控器
     *
     * @param sessionRegistry        会话注册表
     * @param heartbeatTimeoutMillis 空闲超时（毫秒），非正数表示关闭监控
     */
    public HeartbeatMonitor(ISessionRegistry sessionRegistry, long heartbeatTimeoutMillis) {
        this.sessionRegistry = sessionRegistry;
        this.heartbeatTimeoutMillis = heartbeatTimeoutMillis;
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
     */
    private void checkIdleSessions() {
        try {
            long now = System.currentTimeMillis();
            for (IConnectionSession session : sessionRegistry.all()) {
                if (now - session.getLastActiveTime() > heartbeatTimeoutMillis) {
                    log.warn("会话心跳空闲超时, sessionId:{}, transport:{}", session.getSessionId(),
                        session.getTransportType());
                    session.close(PushFrameConstants.CLOSE_REASON_HEARTBEAT_TIMEOUT);
                    sessionRegistry.unregister(session.getSessionId());
                }
            }
        } catch (Throwable error) {
            log.error("心跳空闲检查异常", error);
        }
    }

}
