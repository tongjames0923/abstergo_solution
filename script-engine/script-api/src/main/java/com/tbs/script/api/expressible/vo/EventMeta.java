package com.tbs.script.api.expressible.vo;

import com.tbs.script.api.expressible.vo.param.ParamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 事件元信息：描述一个事件上报器可以上报的事件。
 *
 * <p>eventCode 仅在所属 {@code IEventReporter} 范围内唯一，跨 reporter 不要求唯一；
 * 全局唯一定位一个事件请使用 {@code (reporterId, eventCode)} 组合。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMeta implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 事件编码，在所属 reporter 内唯一
     */
    private String code;

    /**
     * 事件名称（展示用）
     */
    private String name;

    /**
     * 事件展示标签，为空时前端回退使用 name
     */
    private String label;

    /**
     * 事件描述
     */
    private String description;

    /**
     * 该事件的参数声明，用于调用方校验与前端渲染；可为空
     */
    private List<ParamType> paramTypes;
}
