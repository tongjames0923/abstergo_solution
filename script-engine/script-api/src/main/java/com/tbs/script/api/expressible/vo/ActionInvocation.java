package com.tbs.script.api.expressible.vo;

import com.tbs.script.api.expressible.ITriggerAction;
import com.tbs.script.api.expressible.vo.param.ParamValue;

import java.util.Collection;
import java.util.List;

/**
 * 一次动作调用。
 *
 * <p>把「动作实例」与「解析后的参数值」绑定在一起，供触发器回调一次性批量解析、
 * 再逐个执行，避免执行阶段重复查询动作绑定配置。</p>
 *
 * <p>本对象仅在进程内流转，不参与序列化与跨进程传输。</p>
 *
 * @author abstergo
 */
public final class ActionInvocation {

    /**
     * 动作实例
     */
    private final ITriggerAction action;

    /**
     * 动作参数值
     */
    private final Collection<ParamValue> paramValues;

    /**
     * 构造一次动作调用。
     *
     * @param action      动作实例
     * @param paramValues 动作参数值，允许为空
     */
    public ActionInvocation(ITriggerAction action, Collection<ParamValue> paramValues) {
        this.action = action;
        this.paramValues = paramValues == null ? List.of() : List.copyOf(paramValues);
    }

    /**
     * 获取动作实例。
     *
     * @return 动作实例
     */
    public ITriggerAction getAction() {
        return action;
    }

    /**
     * 获取动作参数值。
     *
     * @return 不可变参数值集合
     */
    public Collection<ParamValue> getParamValues() {
        return paramValues;
    }
}
