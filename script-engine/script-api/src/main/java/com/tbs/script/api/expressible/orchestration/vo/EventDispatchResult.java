package com.tbs.script.api.expressible.orchestration.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 事件分发结果。
 *
 * <p>汇总一次分发中所有触发器的执行情况：{@code triggeredCount} 为真正触发的触发器数量，
 * {@code success} 表示所有触发器均被成功调用（未发生异常且均已找到），不代表有触发器命中。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDispatchResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 触发器执行结果列表，顺序与分发顺序一致
     */
    private List<TriggerResult> triggerResults;

    /**
     * 真正触发的触发器数量
     */
    private int triggeredCount;

    /**
     * 是否所有触发器均被成功调用
     */
    private boolean success;

    /**
     * 分发总耗时，单位毫秒
     */
    private long elapsedMs;
}
