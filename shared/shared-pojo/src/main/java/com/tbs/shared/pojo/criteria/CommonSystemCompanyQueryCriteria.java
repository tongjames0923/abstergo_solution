package com.tbs.shared.pojo.criteria;

import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import com.tbs.shared.pojo.model.ClassBaseQueryCriteria;
import com.tbs.sql.annotation.condition.QueryField;
import com.tbs.sql.annotation.mapping.QueryMapping;
import com.tbs.sql.annotation.order.QueryOrderField;
import com.tbs.sql.constants.OrderConstants;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.order.impl.OrderedModel;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 系统公司实体查询条件类 用于构建针对SystemCompanyEntity实体的查询条件，支持多种查询方式和排序
 *
 * @author tongj
 */
@Data
public class CommonSystemCompanyQueryCriteria extends ClassBaseQueryCriteria<SystemCompanyEntity> {
    /**
     * 按ID精确查询
     */
    @QueryField
    @QueryMapping(value = "id")
    private Long equalsId;

    /**
     * 按公司名称模糊查询（左模糊）
     */
    @QueryField(contrast = ContrastEnum.LLIKE)
    @QueryMapping(value = "company_name")
    private String likeName;

    /**
     * 按税号精确查询
     */
    @QueryField
    @QueryMapping(value = "tax_number")
    private String equalsTaxNumber;

    /**
     * 按状态标记IN查询（支持多个状态）
     */
    @QueryField(contrast = ContrastEnum.IN)
    @QueryMapping(value = "flag")
    private List<Integer> flagEquals;

    /**
     * 按创建时间范围查询（起始时间 >=）
     */
    @QueryMapping("create_time")

    @QueryField(contrast = ContrastEnum.GREATER_EQUAL)
    private Date createTimeStart;

    /**
     * 按创建时间范围查询（结束时间 <=）
     */
    @QueryMapping("create_time")
    @QueryField(contrast = ContrastEnum.LESS_EQUAL)

    private Date createTimeEnd;

    /**
     * 按更新时间范围查询（起始时间 >=）
     */
    @QueryMapping("update_time")
    @QueryField(contrast = ContrastEnum.GREATER_EQUAL)

    private Date updateTimeStart;

    /**
     * 按更新时间范围查询（结束时间 <=）
     */
    @QueryMapping("update_time")
    @QueryField(contrast = ContrastEnum.LESS_EQUAL)

    private Date updateTimeEnd;

    /**
     * 创建时间排序字段
     */
    @QueryOrderField(mappedName = "create_time", order = OrderConstants.ORDERED_MODEL)
    private OrderedModel createTimeOrder;

    /**
     * 更新时间排序字段
     */
    @QueryOrderField(mappedName = "update_time", order = OrderConstants.ORDERED_MODEL)

    private OrderedModel updateTimeOrder;

    /**
     * 树节点根标记
     */
    @QueryMapping("tree_root_flag")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Integer treeRootFlag;

}
