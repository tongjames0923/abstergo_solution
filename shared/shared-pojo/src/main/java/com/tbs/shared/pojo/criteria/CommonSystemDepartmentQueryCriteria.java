package com.tbs.shared.pojo.criteria;

import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.sql.annotation.condition.QueryField;
import com.tbs.sql.annotation.mapping.QueryMapping;
import com.tbs.sql.annotation.order.QueryOrderField;
import com.tbs.sql.constants.OrderConstants;
import com.tbs.sql.criteria.impl.NacosConfigQueryCriteria;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.order.impl.OrderedModel;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 系统部门实体查询条件类 用于构建针对SystemDepartmentEntity实体的查询条件，支持多种查询方式和排序
 *
 * @author abstergo
 */
@Data
@LogicDeleteEntity(activeFlagValues = {1, 2})
public class CommonSystemDepartmentQueryCriteria extends NacosConfigQueryCriteria {
    /**
     * 按ID精确查询
     */
    @QueryField
    @QueryMapping(value = "id")
    private Long equalsId;

    /**
     * 按部门名称模糊查询（左模糊）
     */
    @QueryField(contrast = ContrastEnum.LLIKE)
    @QueryMapping(value = "department_name")
    private String likeName;

    /**
     * 按部门编码精确查询
     */
    @QueryField
    @QueryMapping(value = "department_code")
    private String equalsDepartmentCode;

    /**
     * 按公司ID精确查询
     */
    @QueryField
    @QueryMapping(value = "company_id")
    private Long equalsCompanyId;

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

    /**
     * 按ID集合IN查询
     */
    @QueryMapping("id")
    @QueryField(contrast = ContrastEnum.IN)
    private List<Long> idIn;

    /**
     * 按部门名称精确查询
     */
    @QueryField(contrast = ContrastEnum.EQUAL)
    @QueryMapping(value = "department_name")
    private String equalsName;

    /**
     * 按部门编码模糊查询（左模糊）
     */
    @QueryField(contrast = ContrastEnum.LLIKE)
    @QueryMapping(value = "department_code")
    private String likeDepartmentCode;

    /**
     * 按公司ID集合IN查询
     */
    @QueryField(contrast = ContrastEnum.IN)
    @QueryMapping(value = "company_id")
    private List<Long> companyIdIn;

    /**
     * 按部门负责人精确查询
     */
    @QueryField(contrast = ContrastEnum.EQUAL)
    @QueryMapping(value = "manager")
    private String equalsManager;

    /**
     * 按部门负责人模糊查询（左模糊）
     */
    @QueryField(contrast = ContrastEnum.LLIKE)
    @QueryMapping(value = "manager")
    private String likeManager;

    /**
     * 按部门描述模糊查询（左模糊）
     */
    @QueryField(contrast = ContrastEnum.LLIKE)
    @QueryMapping(value = "description")
    private String likeDescription;

    /**
     * 按父部门ID精确查询
     */
    @QueryField(contrast = ContrastEnum.EQUAL)
    @QueryMapping(value = "parent_id")
    private Long equalsParentId;

    /**
     * 按父部门ID集合IN查询
     */
    @QueryField(contrast = ContrastEnum.IN)
    @QueryMapping(value = "parent_id")
    private List<Long> parentIdIn;

    /**
     * 按创建者ID精确查询
     */
    @QueryField(contrast = ContrastEnum.EQUAL)
    @QueryMapping(value = "create_by")
    private Long equalsCreateBy;

    /**
     * 按更新者ID精确查询
     */
    @QueryField(contrast = ContrastEnum.EQUAL)
    @QueryMapping(value = "update_by")
    private Long equalsUpdateBy;

    public CommonSystemDepartmentQueryCriteria() {
        super("com.tbs.shared.pojo.criteria.CommonSystemDepartmentQueryCriteria");
    }
}
