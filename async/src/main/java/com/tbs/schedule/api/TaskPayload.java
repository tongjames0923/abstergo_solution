package com.tbs.schedule.api;

import java.io.Serializable;

/**
 * 任务载荷抽象接口
 * <p>
 * 描述"任务要做什么"的纯数据载体，与执行方式解耦。
 * 调度框架内核不感知载荷内容，仅由对应的 {@link TaskExecutor} 按 {@code taskType} 解释执行。
 * 新增任务类型只需扩展一个 Payload 实现类并注册对应执行器，无需改动核心逻辑。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface TaskPayload extends Serializable {

    /**
     * 声明该载荷是否可被持久化存储（落盘并在进程重启后恢复）
     * <p>
     * 默认 {@code true}：绝大多数载荷为纯数据（JSON / 二进制可序列化），支持可靠持久化。 内存态载荷（如持有 {@link Runnable} 闭包的本地任务）应覆写为 {@code false}，
     * 内核在接入持久化存储时会跳过其落盘，避免序列化失败或重启后无法还原执行行为。
     * </p>
     *
     * @return 可持久化返回 true
     */
    default boolean isPersistable() {
        return true;
    }
}
