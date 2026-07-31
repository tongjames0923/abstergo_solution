package com.tbs.thread;

import com.tbs.common.context.ContextPropagator;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * 抽象线程池管理器
 * <p>
 * 提供线程池任务提交的基础能力，支持通过 {@link ContextPropagator} 将调用线程的上下文
 * （如请求上下文、国际化上下文等）传播到工作线程中。
 * <p>
 * 上下文传播机制：
 * <ul>
 *   <li>在任务提交时，依次调用所有已注册的 ContextPropagator 对 Runnable 进行包装</li>
 *   <li>每个 Propagator 在调用线程中快照上下文，在工作线程中恢复并在 finally 中清理</li>
 *   <li>支持多个上下文的链式传播（如同时传播 RequestDataContext 和 Locale）</li>
 * </ul>
 *
 * @author tongj
 * @see ContextPropagator
 */
public abstract class AbstractThreadPoolManager implements IThreadPoolManager {

    /**
     * 上下文传播器列表，用于在异步任务中传播父线程的上下文
     */
    private List<ContextPropagator> propagators = Collections.emptyList();

    /**
     * 设置上下文传播器
     * <p>
     * 传播器会在每次 submit() 时被调用，将调用线程的上下文传播到工作线程
     *
     * @param propagators 传播器列表，传入null将被视为空列表
     */
    public void setPropagators(List<ContextPropagator> propagators) {
        this.propagators = propagators != null ? new ArrayList<>(propagators) : Collections.emptyList();
    }

    /**
     * 根据名称获取Executor
     *
     * @param executorName
     * @return
     */
    protected abstract Optional<Executor> getExecutorByName(String executorName);

    /**
     * 获取默认Executor
     *
     * @return
     */
    protected abstract Executor getDefaultExecutor();

    /**
     * 根据优先级和耗时获取Executor
     *
     * @param costTime
     * @param priority
     * @return
     */
    protected abstract Optional<Executor> getExecutorByCostAndPriority(int costTime, int priority);

    @Override
    public void submit( IThreadRequest request, IOnThreadError onThreadError) {
        Executor executor = getExecutorByName(request.getExpectedExecutorServiceName()).orElseGet(() -> {
            return getExecutorByCostAndPriority(request.getExpectedCostTime(), request.getPriority()).orElseGet(
                this::getDefaultExecutor);
        });
        AssertUtil.notNull(executor, "未找到ExecutorService");

        // 构造原始任务
        Runnable task = () -> {
            try {
                request.getThreadCall().call(request, this);
            } catch (Throwable e) {
                AssertUtil.notNull(onThreadError,
                    () -> new FormattedRuntimeException(e, "线程执行异常且未提供有效线程异常处理器 {}",
                        e.getLocalizedMessage()));
                onThreadError.onThreadError(e, Thread.currentThread());
            }
        };

        // 通过上下文传播器链式包装任务，将父线程上下文传播到工作线程
        for (ContextPropagator propagator : propagators) {
            task = propagator.wrap(task);
        }

        executor.execute(task);
    }
}
