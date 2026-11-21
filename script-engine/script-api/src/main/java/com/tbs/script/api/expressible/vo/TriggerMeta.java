package com.tbs.script.api.expressible.vo;

import com.tbs.script.api.expressible.vo.param.ParamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 事件触发器元信息。
 *
 * <p>供前端订阅编排页选择触发器：触发器实例不直接暴露给前端，
 * 统一以本对象承载 ID、名称、描述与参数声明。</p>
 *
 * @author abstergo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TriggerMeta implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 触发器ID，全局唯一
     */
    private String triggerId;

    /**
     * 触发器名称（展示用）
     */
    private String triggerName;

    /**
     * 触发器描述
     */
    private String description;

    /**
     * 触发器参数声明；条件触发器默认为空
     */
    private List<ParamType> paramTypes;
}
