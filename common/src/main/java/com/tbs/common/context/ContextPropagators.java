package com.tbs.common.context;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * 上下文传播器工具
 * <p>
 * 提供 {@link ContextPropagator} 链的统一包装入口，供各异步执行路径
 * （托管线程池、Spring {@code @Async}、Spring MVC 异步等）复用，
 * 确保所有异步执行方式都经同一传播管道处理上下文，避免各处自行实现包装逻辑。
 * </p>
 * <p>
 * <b>包装语义</b>：按列表顺序链式调用 {@link ContextPropagator#wrap(Runnable)}——
 * 后包装的传播器位于外层，各传播器独立完成"提交线程快照 → 工作线程恢复 →
 * finally 恢复式清理"。传播器列表为空时原样透传，零开销。
 * </p>
 *
 * @author abstergo
 * @see ContextPropagator
 */
public final class ContextPropagators {

    private ContextPropagators() {
        // 工具类，禁止实例化
    }

    /**
     * 使用传播器链包装任务
     * <p>
     * 按列表顺序依次包装，传播器列表为空或任务为 null 时原样返回。
     * </p>
     *
     * @param propagators 传播器列表，允许为 null 或空
     * @param task        原始任务
     * @return 包装后的任务；无传播器时为原始任务
     */
    public static Runnable wrap(List<ContextPropagator> propagators, Runnable task) {
        if (propagators == null || propagators.isEmpty() || task == null) {
            return task;
        }
        Runnable wrapped = task;
        for (ContextPropagator propagator : propagators) {
            wrapped = propagator.wrap(wrapped);
        }
        return wrapped;
    }

    /**
     * 使用传播器链包装执行器
     * <p>
     * 返回的执行器在每次 {@code execute} 时先经传播器链包装任务再提交，
     * 使不经托管线程池的执行路径（如 Spring {@code @Async}、Spring MVC 异步）
     * 同样具备上下文传播能力。
     * </p>
     *
     * @param propagators 传播器列表，允许为 null 或空
     * @param executor    原始执行器
     * @return 包装后的执行器；无传播器时为原始执行器
     */
    public static Executor wrapExecutor(List<ContextPropagator> propagators, Executor executor) {
        if (propagators == null || propagators.isEmpty()) {
            return executor;
        }
        return command -> executor.execute(wrap(propagators, command));
    }
}
