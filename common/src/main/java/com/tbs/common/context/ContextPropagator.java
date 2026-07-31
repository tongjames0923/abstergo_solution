package com.tbs.common.context;

/**
 * 线程上下文传播器
 * <p>
 * 用于在线程池提交任务时，将调用线程的上下文（如 RequestDataContext、Locale 等）
 * 传播到工作线程中。
 * <p>
 * 工作原理：
 * <pre>{@code
 * // 在提交任务时快照当前线程的上下文
 * // 在工作线程执行前恢复上下文
 * // 在工作线程执行后清理上下文
 * }</pre>
 * <p>
 * 实现类应遵循以下约定：
 * <ul>
 *   <li>wrap() 方法在调用线程（提交线程）中执行，用于捕获上下文快照</li>
 *   <li>返回的 Runnable 在工作线程中执行，负责恢复和清理上下文</li>
 *   <li>必须在 finally 块中清理上下文，防止线程池复用时残留脏数据</li>
 * </ul>
 * <p>
 * 典型实现示例：
 * <pre>{@code
 * public class MyContextPropagator implements ContextPropagator {
 *     @Override
 *     public Runnable wrap(Runnable task) {
 *         // 在调用线程中捕获上下文
 *         MyContext ctx = MyContextHolder.get();
 *         return () -> {
 *             try {
 *                 // 在工作线程中恢复上下文
 *                 MyContextHolder.set(ctx);
 *                 task.run();
 *             } finally {
 *                 // 清理，防止线程复用时残留
 *                 MyContextHolder.clear();
 *             }
 *         };
 *     }
 * }
 * }</pre>
 *
 * @author abstergo
 * @see com.tbs.common.i18n.I18nContextHolder
 */
public interface ContextPropagator {

    /**
     * 包装 Runnable，在执行前后传播上下文
     * <p>
     * 此方法在调用线程（通常是请求线程）中执行，
     * 返回的 Runnable 将在线程池的工作线程中执行。
     *
     * @param task 原始任务
     * @return 包装后的任务，包含上下文传播逻辑
     */
    Runnable wrap(Runnable task);
}
