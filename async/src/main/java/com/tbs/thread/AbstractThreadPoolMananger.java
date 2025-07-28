package com.tbs.thread;

import com.tbs.common.delegate.IDelegateTemplate;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.StrUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * @author tongj
 */
public abstract class AbstractThreadPoolMananger implements IThreadPoolManager {

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
    public void submit(@NotNull IThreadRequest request, IOnThreadError onThreadError) {
        Executor executor = getExecutorByName(request.getExpectedExecutorServiceName()).orElseGet(() -> {
            return getExecutorByCostAndPriority(request.getExpectedCostTime(), request.getPriority()).orElseGet(
                this::getDefaultExecutor);
        });
        AssertUtil.notNull(executor, "未找到ExecutorService");
        executor.execute(() -> {
            try {
                request.getThreadCall().call(request, this);
            } catch (Throwable e) {
                AssertUtil.notNull(onThreadError,
                    () -> new FormattedRuntimeException(e, "线程执行异常且未提供有效线程异常处理器 {}",
                        e.getLocalizedMessage()));
                onThreadError.onThreadError(e, Thread.currentThread());
            }
        });
    }
}
