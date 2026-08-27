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
}
