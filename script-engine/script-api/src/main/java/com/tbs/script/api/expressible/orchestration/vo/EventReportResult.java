package com.tbs.script.api.expressible.orchestration.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 事件上报编排结果。
 *
 * <p>{@code reported} 表示上报器调用是否成功；上报失败时不再分发，{@code dispatch} 为空。
 * {@code success} 表示上报成功且分发的所有触发器均被成功调用。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventReportResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 上报器是否上报成功
     */
    private boolean reported;

    /**
     * 上报失败原因，成功时为空
     */
    private String errorMsg;

    /**
     * 分发结果，上报失败时为空
     */
    private EventDispatchResult dispatch;

    /**
     * 整体是否成功：上报成功且所有触发器均被成功调用
     */
    private boolean success;

    /**
     * 总耗时，单位毫秒
     */
    private long elapsedMs;
}
