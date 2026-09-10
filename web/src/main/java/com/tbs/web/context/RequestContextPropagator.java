package com.tbs.web.context;

import com.tbs.common.context.ContextPropagator;

/**
 * 请求上下文传播器
 * <p>
 * 将提交线程的 {@link RequestDataContext} 传播到线程池的工作线程中，
 * 是 {@link RequestContextHolder} 线程间传播的唯一途径。
 * <p>
 * 解决的问题：
 * {@link RequestContextHolder} 基于普通 {@link ThreadLocal}，工作线程无法读到
 * 提交线程的上下文。本传播器在提交任务时快照 {@link RequestDataContext}，
 * 在工作线程执行前恢复、finally 清理，确保异步任务能正确使用请求上下文数据
 * （如 traceId、业务数据等），且线程复用时不残留。
 * <p>
 * <b>契约说明</b>：
 * <ul>
 *   <li>快照发生在任务提交时（持有上下文对象引用），请求线程后续清理线程本地存储
 *       不影响工作线程中的快照使用；</li>
 *   <li>快照为对象引用而非深拷贝：请求线程与工作线程共享同一上下文实例，
 *       {@code businessData} 非线程安全，两端并发读写需自行保证同步；</li>
 *   <li>仅快照非 null 时恢复，finally 中<b>恢复式清理</b>（还原工作线程执行前的
 *       状态）——任务因线程池拒绝策略回退到提交线程同步执行（CallerRunsPolicy）时，
 *       不会误清提交线程自身的上下文。</li>
 * </ul>
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
            // 记录工作线程原有上下文，支持恢复式清理
            RequestDataContext previous = RequestContextHolder.get();
            try {
                // 在工作线程中恢复请求上下文
                if (context != null) {
                    RequestContextHolder.set(context);
                }
                task.run();
            } finally {
                // 恢复工作线程原有状态：任务被拒绝回退到提交线程执行（CallerRunsPolicy）时，
                // 不会误清提交线程自身的上下文
                RequestContextHolder.set(previous);
            }
        };
    }
}
