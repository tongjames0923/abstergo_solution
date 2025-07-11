package com.tbs.async.util;

import com.tbs.async.ThreadCallableTask;
import com.tbs.common.function.SBiConsumer;
import com.tbs.common.util.SpringUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * 线程工具类
 *
 * <p>该类提供了一系列用于处理线程和任务的静态方法。通过此类，可以方便地执行异步操作、批量任务等。
 *
 * @author abstergo
 */
@Component
public class ThreadUtil {

    @Resource
    private ExecutorService executorService;

    /**
     * 线程工具实例
     */
    private static ThreadUtil threadUtil;

    /**
     * 获取线程工具实例
     *
     * <p>该方法用于获取线程工具类的唯一实例。
     *
     * @return 线程工具实例
     */
    public static ThreadUtil getInstance() {
        return SpringUtil.getBean(ThreadUtil.class);
    }

    /**
     * 获取执行器
     *
     * <p>该方法用于获取线程池执行器。
     *
     * @return 执行器
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * 运行任务
     *
     * <p>该方法用于在后台运行一个或多个任务。
     *
     * @param runnables 要运行的任务
     */
    public void runCollectionInBackground(Runnable... runnables) {
        for (Runnable runnable : runnables) {
            getExecutorService().execute(runnable);
        }
    }

    /**
     * 批量运行任务
     *
     * <p>该方法用于在后台批量运行任务。
     *
     * @param runnables 要运行的任务集合
     */
    public void runCollectionInBackground(Collection<Runnable> runnables) {
        for (Runnable runnable : runnables) {
            getExecutorService().execute(runnable);
        }
    }

    /**
     * 运行异步任务
     *
     * <p>该方法用于运行异步任务并返回结果。
     *
     * @param tasks   要运行的任务列表
     * @param timeout 任务执行超时时间
     * @param unit    超时时间单位
     * @param <T>     任务返回结果类型
     * @return 任务结果列表
     * @throws InterruptedException 中断异常
     */
    public <T> List<Future<T>> callAndAwait(List<Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException {
        return getExecutorService().invokeAll(tasks, timeout, unit);
    }

    /**
     * 运行异步任务
     *
     * <p>该方法用于运行异步任务并返回结果。
     *
     * @param tasks 要运行任务的列表
     * @param <T>   任务返回结果类型
     * @return 任务结果列表
     */
    public <T, P> ThreadCallableTask<T, P> asyncCall(T t, SBiConsumer<T, P> function, Supplier<P> supplier) {
        ThreadCallableTask<T, P> task = new ThreadCallableTask<>(t, function, supplier);
        task.setFuture(CompletableFuture.supplyAsync(() -> {
            function.accept(t, supplier.get());
            return t;
        }, getExecutorService()));
        return task;
    }

    /**
     * 创建异步任务
     *
     * <p>该方法用于创建异步任务。
     *
     * @param t   任务参数
     * @param <T> 任务参数类型
     * @param <P> 任务返回结果类型
     * @return 异步任务构建器
     */
    public <T> ThreadCallableTask.Builder<T> createAsyncCall(T t) {
        return new ThreadCallableTask.Builder<T>(t);
    }

}
