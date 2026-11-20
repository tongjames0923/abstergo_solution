package com.tbs.shared.pojo.criteria;

import com.tbs.shared.pojo.entity.condition.SystemConditionRuleEntity;
import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.sql.annotation.condition.QueryField;
import com.tbs.sql.annotation.mapping.QueryMapping;
import com.tbs.sql.annotation.order.QueryOrderField;
import com.tbs.sql.constants.OrderConstants;
import com.tbs.sql.criteria.impl.DefaultEntityCriteria;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.order.impl.OrderedModel;
import lombok.Data;

/**
 * 条件规则查询条件
 *
 * @author abstergo
 */
@Data
@LogicDeleteEntity(activeFlagValues = {1})
public class ConditionRuleCriteria extends DefaultEntityCriteria {

    public ConditionRuleCriteria() {
        super(SystemConditionRuleEntity.class);
    }

    /**
     * 按主键精确查询
     */
    @QueryMapping(value = "id")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Long idEquals;

    /**
     * 按规则编码模糊查询
     */
    @QueryMapping(value = "rule_code")
    @QueryField(contrast = ContrastEnum.LLIKE)
    private String ruleCodeLike;

    /**
     * 按规则名称模糊查询
     */
    @QueryMapping(value = "rule_name")
    @QueryField(contrast = ContrastEnum.LLIKE)
    private String ruleNameLike;

    /**
     * 按启用状态精确查询
     */
    @QueryMapping(value = "enabled")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Integer enabledEquals;

    /**
     * 按创建时间排序
     */
    @QueryOrderField(mappedName = "create_time", order = OrderConstants.ORDERED_MODEL)
    private OrderedModel createTimeOrder;
}
