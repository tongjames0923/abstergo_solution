package com.tbs.schedule.dispatcher;

import com.tbs.common.util.framework.SpringUtil;
import com.tbs.schedule.api.*;
import com.tbs.schedule.executor.ExecutionPolicyResolver;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 本地任务派发器（默认实现）
 * <p>
 * 将到期任务提交到本地线程池执行，解决原内核在轮询线程内串行执行导致慢任务拖垮整批 TTL 生效的问题。 执行线程池支持按 {@link ExecutionPolicy#getIsolationGroup()} 隔离分组；分组解析由
 * {@link ExecutionPolicyResolver} 完成。 执行层的异常处理、状态回调（成功/失败）统一在此编排，与调度时序解耦。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class LocalTaskDispatcher implements TaskDispatcher {

    /**
     * 默认执行线程池
     */
    private final Executor executor;

    /**
     * 执行策略解析器
     */
    private final ExecutionPolicyResolver policyResolver;

    /**
     * 生命周期监听器集合
     */
    private final List<LifecycleListener> listeners;

    /**
     * 构造本地派发器
     *
     * @param policyResolver 执行策略解析器
     * @param listeners      生命周期监听器
     */
    public LocalTaskDispatcher(ExecutionPolicyResolver policyResolver, List<LifecycleListener> listeners) {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger seq = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "schedule-dispatcher-" + seq.getAndIncrement());
                t.setDaemon(true);
                return t;
            }
        };
        this.executor = Executors.newCachedThreadPool(threadFactory);
        this.policyResolver = policyResolver;
        this.listeners = listeners == null ? List.of() : listeners;
    }

    @Override
    public void dispatch(TaskSubmission submission) {
        ExecutionPolicy policy = policyResolver.resolve(submission);
        String isolationGroup = policy.getIsolationGroup();
        log.info("调度派发任务: taskId={}, taskType={}, isolationGroup={}", submission.getTaskId(),
            submission.getTaskType(), isolationGroup);
        listeners.forEach(l -> l.onDispatched(submission));
        executor.execute(() -> {
            long start = System.currentTimeMillis();
            String threadName = Thread.currentThread().getName();
            try {
                TaskExecutor taskExecutor = resolveExecutor(submission.getTaskType());
                log.debug("任务开始执行: taskId={}, threadName={}", submission.getTaskId(), threadName);
                listeners.forEach(l -> l.onRunning(submission));
                taskExecutor.execute(submission);
                long cost = System.currentTimeMillis() - start;
                log.info("任务执行成功: taskId={}, threadName={}, costMs={}", submission.getTaskId(), threadName, cost);
                listeners.forEach(l -> l.onSucceeded(submission));
            } catch (Throwable e) {
                long cost = System.currentTimeMillis() - start;
                log.error("任务执行失败: taskId={}, threadName={}, costMs={}", submission.getTaskId(), threadName, cost, e);
                listeners.forEach(l -> l.onFailed(submission, e));
                policy.getErrorHandle().handle(submission, e);
            }
        });
    }

    /**
     * 按任务类型从 Spring 容器解析执行器
     *
     * @param taskType 任务类型
     * @return 匹配的执行器
     */
    @NotNull
    private TaskExecutor resolveExecutor(String taskType) {
        Collection<TaskExecutor> executors = SpringUtil.getBeansByTypeSafe(TaskExecutor.class);
        return executors.stream().filter(ex -> ex.supports(taskType)).findFirst()
            .orElseThrow(() -> new IllegalStateException("no executor for type: " + taskType));
    }
}
