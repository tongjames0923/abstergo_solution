package com.tbs.script.api.expressible.vo;

import com.tbs.script.api.expressible.vo.param.ParamType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 触发器动作元信息。
 *
 * <p>供前端编排页展示可选动作与动态渲染参数表单：动作实例不直接暴露给前端，
 * 统一以本对象承载 ID、名称、描述、执行顺序与参数声明。</p>
 *
 * @author abstergo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionMeta implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 动作ID，全局唯一
     */
    private String actionId;

    /**
     * 动作名称（展示用）
     */
    private String actionName;

    /**
     * 动作描述
     */
    private String actionDesc;

    /**
     * 默认执行顺序，升序；为空表示排在末尾
     */
    private Integer order;

    /**
     * 动作参数声明，前端据此渲染表单；可为空
     */
    private List<ParamType> paramTypes;
}
