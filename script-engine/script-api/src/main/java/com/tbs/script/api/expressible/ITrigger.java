package com.tbs.script.api.expressible;

import com.tbs.common.provider.IRouteMeta;
import com.tbs.common.provider.vo.InstanceMeta;
import com.tbs.script.api.expressible.vo.param.ParamType;
import com.tbs.script.api.expressible.vo.param.ParamValue;

import java.util.Collection;
import java.util.List;

/**
 * 事件触发器接口。
 *
 * <p>(reporterId, eventCode) 唯一标识一个事件；触发器接收该组合后自行判断是否满足条件，
 * 满足时执行触发逻辑。</p>
 *
 * <p><b>推荐调用方式：</b>直接调用 {@link #trigger(String, String, Collection)}，
 * 由实现内部一次性完成"判断 + 执行"，返回值表示是否真正触发。 这样可以避免 isTrigger + trigger 双调用带来的重复计算与状态不一致（TOCTOU）。
 * {@link #isTrigger(String, String, Collection)} 仅用于"只查询不执行"的预览场景。</p>
 */
public interface ITrigger extends IRouteMeta {

    /**
     * 尝试触发。实现内部先判断是否满足触发条件，满足则执行触发并返回 {@code true}， 不满足则直接返回 {@code false}，不产生副作用。
     *
     * @param reporterId  事件上报器ID
     * @param eventCode   事件编码，仅在 reporterId 范围内唯一
     * @param paramValues 事件参数值
     * @return 是否真正触发
     */
    boolean trigger(String reporterId, String eventCode, Collection<ParamValue> paramValues);

    /**
     * 仅查询是否满足触发条件，<b>不产生任何副作用</b>，用于预览/调试场景。
     *
     * <p>若调用方在查询后紧接着要执行触发，请直接调用 {@link #trigger(String, String, Collection)}，
     * 不要 isTrigger + trigger 双调用。</p>
     */
    boolean isTrigger(String reporterId, String eventCode, Collection<ParamValue> paramValues);

    /**
     * 触发器声明的参数定义，用于调用方校验与前端渲染。
     * <p>默认为空列表，表示触发器不约束参数。</p>
     */
    default List<ParamType> requestParamTypes() {
        return List.of();
    }

    /**
     * 获取触发器ID，作为路由 business，全局唯一。
     */
    String getTriggerId();

    /**
     * 获取触发器名称（展示用）。
     */
    String getTriggerName();

    /**
     * 获取触发器描述。
     */
    String getDescription();

    @Override
    default InstanceMeta getMeta() {
        return IRouteMeta.super.getMeta().setBusiness(getTriggerId());
    }
}
