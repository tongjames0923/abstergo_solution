package com.tbs.script.api.expressible;

import com.tbs.script.api.expressible.vo.param.ParamType;

import java.util.List;

public interface IRequestable {
    /**
     * 触发器声明的参数定义，用于调用方校验与前端渲染。
     * <p>默认为空列表，表示触发器不约束参数。</p>
     */
    default List<ParamType> requestParamTypes() {
        return List.of();
    }
}
