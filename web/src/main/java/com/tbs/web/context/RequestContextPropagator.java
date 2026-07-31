package com.tbs.web.context;

import com.tbs.common.context.ContextPropagator;

/**
 * 请求上下文传播器
 * <p>
 * 将父线程的 {@link RequestDataContext} 信息传播到线程池的工作线程中。
 * <p>
 * 解决的问题：
 * 在异步线程池场景下，{@link RequestContextHolder} 使用的是 InheritableThreadLocal，
 * 但 InheritableThreadLocal 只在线程首次创建时从父线程拷贝值。当线程池复用线程时，
 * 工作线程拿到的是旧值（上一个请求的残留）甚至是空值。
 * <p>
 * 本传播器通过在提交任务时快照 {@link RequestDataContext}，
 * 在工作线程执行前恢复、执行后清理，确保异步任务能正确使用请求上下文数据（如 traceId、业务数据等）。
 * <p>
 * 前提条件：
 * 调用方需保证异步任务在请求线程存续期间完成（即请求线程不会在异步任务执行前被清理上下文）。
 *
 * @author abstergo
 * @see ContextPropagator
 * @see RequestContextHolder
 * @see RequestDataContext
 */
public class RequestContextPropagator implements ContextPropagator {

    @Override
    public Runnable wrap(Runnable task) {
        // 在调用线程（请求线程）中快照当前请求上下文
        RequestDataContext context = RequestContextHolder.get();
        return () -> {
            try {
                // 在工作线程中恢复请求上下文
                if (context != null) {
                    RequestContextHolder.set(context);
                }
                task.run();
            } finally {
                // 清理，防止线程池复用时残留上一个请求的上下文
                RequestContextHolder.clear();
            }
        };
    }
}
