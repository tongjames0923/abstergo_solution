package com.tbs.script.api.expressible.orchestration.vo;

import com.tbs.script.api.expressible.vo.param.ParamValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 事件上报信封。
 *
 * <p>一次编排调用的完整入参：(reporterId, eventCode) 全局定位一个事件，
 * paramValues 为该事件的参数值，顺序与数量应满足事件声明的参数定义。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope implements Serializable {
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
     * 事件参数值
     */
    private List<ParamValue> paramValues;
}
