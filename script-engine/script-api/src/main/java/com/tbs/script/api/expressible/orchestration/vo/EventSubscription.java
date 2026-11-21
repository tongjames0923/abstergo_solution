package com.tbs.script.api.expressible.orchestration.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 事件订阅关系。
 *
 * <p>描述「某个事件由哪个触发器处理」：{@code (reporterId, eventCode)} 定位事件，{@code triggerId} 指定触发器。
 * {@code ruleCode} 为该绑定额外的条件规则编码，条件触发器据此判断是否真正触发；为空表示不做条件判定，
 * 由触发器自身决定触发语义。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSubscription implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 事件上报器ID
     */
    private String reporterId;

    /**
     * 事件编码，仅在所属上报器内唯一
     */
    private String eventCode;

    /**
     * 触发器ID
     */
    private String triggerId;

    /**
     * 条件规则编码，为空表示该绑定不做条件判定
     */
    private String ruleCode;

    /**
     * 执行顺序，升序；为空时排在同事件其余订阅之后
     */
    private Integer order;
}
