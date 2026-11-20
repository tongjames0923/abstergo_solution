package com.tbs.app.service.condition;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.condition.SystemConditionRuleEntity;

/**
 * 条件规则服务。
 *
 * <p>把值获取器编排出的判断逻辑持久化为按编码索引的规则，并支持在任意时刻按编码求值：
 * 服务端内部可直接调用 {@link #evaluate(String)}，HTTP 层通过控制器转出同一个结果。</p>
 *
 * @author abstergo
 */
public interface IConditionRuleService extends IServiceTemplate<SystemConditionRuleEntity, Long> {

    /**
     * 按规则编码查询未删除的规则，不区分启用状态。
     *
     * @param ruleCode 规则编码
     * @return 规则实体；不存在时返回 null
     */
    SystemConditionRuleEntity queryByCode(String ruleCode);

    /**
     * 启用或停用规则。
     *
     * @param id      规则主键
     * @param enabled true 启用，false 停用
     */
    void setEnabled(Long id, boolean enabled);

    /**
     * 按规则编码逻辑删除规则。
     *
     * @param ruleCode 规则编码
     */
    void deleteByCode(String ruleCode);
}
