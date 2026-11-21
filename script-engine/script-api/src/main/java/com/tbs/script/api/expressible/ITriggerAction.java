package com.tbs.script.api.expressible;

import com.tbs.common.provider.IRouteMeta;
import com.tbs.common.provider.vo.InstanceMeta;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import org.springframework.core.Ordered;

import java.util.Collection;

/**
 * 触发器动作。
 *
 * <p>触发器动作由触发器触发，执行具体业务动作。</p>
 */
public interface ITriggerAction extends IRouteMeta, IRequestable, Ordered {

    /**
     * 获取动作名称
     * @return 返回动作名称
     */
    public String getActionName();

    /**
     * 获取动作ID
     * @return 返回动作ID
     */
    public String getActionId();

    /**
     * 获取动作描述
     * @return 返回动作描述
     */
    public String getActionDesc();

    /**
     * 执行动作
     * @param paramValues 参数值
     */
    public void execute(Collection<ParamValue> paramValues);

    @Override
    default InstanceMeta getMeta() {
        return IRouteMeta.super.getMeta().setBusiness(getActionId());
    }
}
