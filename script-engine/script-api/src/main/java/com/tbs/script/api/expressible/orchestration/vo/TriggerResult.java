package com.tbs.script.api.expressible.orchestration.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 单个触发器的执行结果。
 *
 * <p>{@code triggered} 表示触发器是否真正触发；{@code success} 表示调用过程是否未发生异常。
 * 规则未命中属于正常未触发（{@code triggered=false, success=true}），触发异常则为失败。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 触发器ID
     */
    private String triggerId;

    /**
     * 触发器名称，展示用
     */
    private String triggerName;

    /**
     * 是否真正触发
     */
    private boolean triggered;

    /**
     * 调用过程是否成功，未找到触发器或执行异常时为 false
     */
    private boolean success;

    /**
     * 失败原因，成功时为空
     */
    private String errorMsg;

    /**
     * 调用耗时，单位毫秒
     */
    private long elapsedMs;
}
