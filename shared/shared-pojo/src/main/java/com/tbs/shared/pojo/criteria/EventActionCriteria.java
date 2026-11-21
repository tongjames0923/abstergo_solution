package com.tbs.shared.pojo.criteria;

import com.tbs.shared.pojo.entity.event.SystemEventActionEntity;
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
 * 事件动作绑定查询条件
 *
 * @author abstergo
 */
@Data
@LogicDeleteEntity(activeFlagValues = {1})
public class EventActionCriteria extends DefaultEntityCriteria {

    public EventActionCriteria() {
        super(SystemEventActionEntity.class);
    }

    /**
     * 按主键精确查询
     */
    @QueryMapping(value = "id")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Long idEquals;

    /**
     * 按上报器ID精确查询
     */
    @QueryMapping(value = "reporter_id")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String reporterIdEquals;

    /**
     * 按上报器ID模糊查询
     */
    @QueryMapping(value = "reporter_id")
    @QueryField(contrast = ContrastEnum.LLIKE)
    private String reporterIdLike;

    /**
     * 按事件编码精确查询
     */
    @QueryMapping(value = "event_code")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String eventCodeEquals;

    /**
     * 按事件编码模糊查询
     */
    @QueryMapping(value = "event_code")
    @QueryField(contrast = ContrastEnum.LLIKE)
    private String eventCodeLike;

    /**
     * 按动作ID精确查询
     */
    @QueryMapping(value = "action_id")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String actionIdEquals;

    /**
     * 按启用状态精确查询
     */
    @QueryMapping(value = "enabled")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Integer enabledEquals;

    /**
     * 按顺序升序排序
     */
    @QueryOrderField(mappedName = "sort_order", order = OrderConstants.ORDERED_MODEL)
    private OrderedModel sortOrderOrder;

    /**
     * 按创建时间排序
     */
    @QueryOrderField(mappedName = "create_time", order = OrderConstants.ORDERED_MODEL)
    private OrderedModel createTimeOrder;
}
