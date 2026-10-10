package com.tbs.cdc.bus;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tbs.cdc.bridge.CdcMqBridge;
import com.tbs.cdc.filter.CdcEventFilter;
import com.tbs.cdc.i18n.CdcI18nConstant;
import com.tbs.cdc.model.CdcEvent;
import com.tbs.cdc.property.CdcProperty;
import com.tbs.cdc.publisher.CdcEventPublisher;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.framework.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 进程内事件总线
 * <p>
 * CDC事件分发管道入口，实现{@link CdcEventPublisher}。事件经滤链后按配置分发：
 * <ul>
 *   <li>同步模式：在捕获线程内即时路由分发</li>
 *   <li>异步模式：入有界队列，由独立刷新线程按批量/间隔刷出，队列满按背压策略处理</li>
 * </ul>
 * 分发时并行投递至命中的进程内监听器（按监听async标志走独立线程池或内联执行）与可选MQ桥接。
 * 监听器执行失败按{@code retry-times}/{@code retry-delay}重试，最终失败按{@code on-fail}策略处理。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "tbs.cdc", name = "enable", havingValue = "true")
public class CdcEventBus implements CdcEventPublisher {

    @Resource
    private CdcProperty cdcProperty;

    @Resource
    private CdcEventFilter cdcEventFilter;

    @Resource
    private CdcListenerRegistry cdcListenerRegistry;

    @Resource
    private CdcMqBridge cdcMqBridge;

    @Resource
    private UuidUtil uuidUtil;

    /**
     * 异步模式有界分发队列
     */
    private ArrayBlockingQueue<CdcEvent> queue;

    /**
     * 异步监听执行线程池
     */
    private ThreadPoolExecutor listenerExecutor;

    /**
     * 异步刷新线程
     */
    private Thread flusherThread;

    /**
     * 运行标志
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 队列序号生成，用于命名刷新线程
     */
    private final AtomicLong threadSeq = new AtomicLong();

    /**
     * 按配置启动总线：异步模式创建队列、线程池与刷新线程
     */
    public void start() {
        if (running.get()) {
            return;
        }
        CdcProperty.Batch batch = cdcProperty.getBatch();
        boolean async = CdcProperty.InProcessMode.ASYNC.getValue().equalsIgnoreCase(
            cdcProperty.getDispatch().getInProcessMode());
        if (async) {
            this.queue = new ArrayBlockingQueue<>(Math.max(1, batch.getQueueCapacity()));
            this.listenerExecutor = newExecutor();
            this.flusherThread = new Thread(this::flushLoop, "cdc-bus-flusher-" + threadSeq.incrementAndGet());
            this.flusherThread.setDaemon(true);
            this.flusherThread.start();
        } else {
            this.listenerExecutor = newExecutor();
        }
        running.set(true);
        log.info("CDC事件总线已启动, in-process-mode:{}, dispatch-mode:{}, queue-capacity:{}",
            cdcProperty.getDispatch().getInProcessMode(), cdcProperty.getDispatch().getMode(),
            async ? batch.getQueueCapacity() : 0);
    }

    /**
     * 停止总线：停止刷新线程并将残余队列刷空，最后关闭监听线程池
     * <p>
     * 调用方须先停止捕获源再调用本方法，保证不再有新事件入队后方可排空。
     * </p>
     */
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        if (flusherThread != null) {
            flusherThread.interrupt();
            try {
                flusherThread.join(TimeUnit.SECONDS.toMillis(10));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        // 排空剩余队列
        drainAndDispatchAll();
        if (listenerExecutor != null) {
            listenerExecutor.shutdown();
            try {
                if (!listenerExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    listenerExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                listenerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("CDC事件总线已停止");
    }

    /**
     * 发布一条变更事件进入分发管道
     *
     * @param event 统一变更事件，不得为null
     */
    @Override
    public void publish(CdcEvent event) {
        AssertUtil.isTrue(event == null).i18n(CdcI18nConstant.EVENT_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        // 统一补齐幂等键与捕获时间戳，覆盖捕获源与人工触发等所有发布路径
        if (StrUtil.isBlank(event.getEventId())) {
            event.setEventId(uuidUtil.getUuidString());
        }
        if (event.getCaptureTimestamp() == null) {
            event.setCaptureTimestamp(System.currentTimeMillis());
        }
        if (!cdcEventFilter.accepts(event)) {
            return;
        }
        boolean async = CdcProperty.InProcessMode.ASYNC.getValue().equalsIgnoreCase(
            cdcProperty.getDispatch().getInProcessMode());
        if (async && queue != null) {
            enqueue(event);
        } else {
            dispatchEvent(event);
        }
    }

    /**
     * 入队并按背压策略处理队列满
     *
     * @param event 变更事件
     */
    private void enqueue(CdcEvent event) {
        String onFull = cdcProperty.getBatch().getOnFull();
        try {
            if (CdcProperty.OnFullPolicy.DROP_OLDEST.getValue().equalsIgnoreCase(onFull)) {
                offerWithDropOldest(event);
            } else {
                // block策略：阻塞直至有空位，保证不丢
                queue.put(event);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("CDC事件入队被中断, eventId:{}", event.getEventId());
        }
    }

    /**
     * drop-oldest背压：队满时丢弃最旧事件并告警，再放入新事件
     *
     * @param event 变更事件
     */
    private void offerWithDropOldest(CdcEvent event) {
        while (!queue.offer(event)) {
            CdcEvent dropped = queue.poll();
            if (dropped != null) {
                log.warn("CDC分发队列已满, 按drop-oldest丢弃最旧事件, eventId:{}", dropped.getEventId());
            } else {
                // 队列已被刷新线程取空，让出CPU后重试
                Thread.onSpinWait();
            }
        }
    }

    /**
     * 刷新线程主循环：按批量大小与最大间隔从队列取事件并分发
     */
    private void flushLoop() {
        CdcProperty.Batch batch = cdcProperty.getBatch();
        int batchSize = Math.max(1, batch.getBatchSize());
        long flushIntervalMs = Math.max(1L, batch.getFlushIntervalMs());
        List<CdcEvent> buffer = new ArrayList<>(batchSize);
        while (running.get()) {
            try {
                CdcEvent first = queue.poll(flushIntervalMs, TimeUnit.MILLISECONDS);
                if (first == null) {
                    continue;
                }
                buffer.add(first);
                queue.drainTo(buffer, batchSize - 1);
                for (CdcEvent e : buffer) {
                    dispatchEvent(e);
                }
            } catch (InterruptedException e) {
                // 停止信号：退出前把已取出的buffer刷出
                Thread.currentThread().interrupt();
                break;
            } finally {
                buffer.clear();
            }
        }
    }

    /**
     * 停止后一次性排空队列剩余事件
     */
    private void drainAndDispatchAll() {
        if (queue == null) {
            return;
        }
        List<CdcEvent> remaining = new ArrayList<>();
        queue.drainTo(remaining);
        for (CdcEvent event : remaining) {
            dispatchEvent(event);
        }
    }

    /**
     * 分发单个事件：桥接MQ + 路由进程内监听器
     *
     * @param event 变更事件
     */
    private void dispatchEvent(CdcEvent event) {
        cdcMqBridge.send(event);
        List<CdcListenerRegistry.ListenerRoute> routes = cdcListenerRegistry.match(event);
        if (CollUtil.isEmpty(routes)) {
            return;
        }
        for (CdcListenerRegistry.ListenerRoute route : routes) {
            submitRoute(route, event);
        }
    }

    /**
     * 按监听async标志提交执行：异步走线程池，同步内联执行
     *
     * @param route 监听路由
     * @param event 变更事件
     */
    private void submitRoute(CdcListenerRegistry.ListenerRoute route, CdcEvent event) {
        if (route.isAsync() && listenerExecutor != null) {
            listenerExecutor.execute(() -> invokeWithRetry(route, event));
        } else {
            invokeWithRetry(route, event);
        }
    }

    /**
     * 执行监听并应用重试与最终失败策略
     *
     * @param route 监听路由
     * @param event 变更事件
     */
    private void invokeWithRetry(CdcListenerRegistry.ListenerRoute route, CdcEvent event) {
        CdcProperty.Error error = cdcProperty.getError();
        int maxAttempts = Math.max(1, error.getRetryTimes() + 1);
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                route.invoke(event);
                return;
            } catch (Exception e) {
                boolean lastAttempt = attempt == maxAttempts;
                if (!lastAttempt) {
                    log.warn("CDC监听器执行失败, 准备重试({}/{}), eventId:{}, listener:{}",
                        attempt, maxAttempts, event.getEventId(), route.getDescription(), e);
                    sleepQuietly(error.getRetryDelayMs());
                } else {
                    handleFinalFail(route, event, e, error);
                }
            }
        }
    }

    /**
     * 最终失败处理：skip记日志，dead-letter记录含事件上下文与载荷快照的死信日志
     *
     * @param route 监听路由
     * @param event 变更事件
     * @param cause 失败异常
     * @param error 错误配置
     */
    private void handleFinalFail(CdcListenerRegistry.ListenerRoute route, CdcEvent event, Exception cause,
        CdcProperty.Error error) {
        if (CdcProperty.OnFailPolicy.DEAD_LETTER.getValue().equalsIgnoreCase(error.getOnFail())) {
            // ponytail: 死信以ERROR日志快照落盘（含eventId/actionable上下文与完整载荷），
            // 升级路径：接入mq-api失败消息存储实现可查询、可重投递的死信队列
            log.error("CDC监听器最终失败-死信快照, eventId:{}, listener:{}, event:{}",
                event.getEventId(), route.getDescription(), com.tbs.common.util.base.JsonUtils.toJSONString(event),
                cause);
        } else {
            log.error("CDC监听器最终失败-跳过, eventId:{}, listener:{}",
                event.getEventId(), route.getDescription(), cause);
        }
    }

    /**
     * 静默休眠，被打断则恢复中断标志
     *
     * @param millis 毫秒
     */
    private void sleepQuietly(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 创建监听执行线程池（有界队列+调用者运行策略，防止监听堆积打爆内存）
     *
     * @return 线程池执行器
     */
    private ThreadPoolExecutor newExecutor() {
        int core = Math.max(2, Runtime.getRuntime().availableProcessors());
        // ponytail: 监听线程池上限取core*4，有界队列2000，饱和时调用者运行形成背压；
        // 升级路径：如需精细隔离可按监听器拆分独立线程池
        return new ThreadPoolExecutor(core, core * 4, 60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(2000),
            r -> {
                Thread t = new Thread(r, "cdc-listener-" + threadSeq.incrementAndGet());
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
