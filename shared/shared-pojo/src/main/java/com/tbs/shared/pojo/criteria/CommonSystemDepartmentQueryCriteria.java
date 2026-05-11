package com.tbs.shared.pojo.criteria;

import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.shared.pojo.model.ClassBaseQueryCriteria;
import com.tbs.sql.annotation.QueryField;
import com.tbs.sql.annotation.QueryMapping;
import com.tbs.sql.annotation.QueryOrderField;
import com.tbs.sql.constants.OrderConstant;
import com.tbs.sql.enums.QueryContrastEnum;
import com.tbs.sql.order.impl.QueryOrderedModel;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 系统部门实体查询条件类 用于构建针对SystemDepartmentEntity实体的查询条件，支持多种查询方式和排序
 *
 * @author abstergo
 */
@Data
public class CommonSystemDepartmentQueryCriteria extends ClassBaseQueryCriteria<SystemDepartmentEntity> {
    /**
     * 按ID精确查询
     */
    @QueryField
    @QueryMapping(value = "id")
    private Long equalsId;

    /**
     * 按部门名称模糊查询（左模糊）
     */
    @QueryField(contrast = QueryContrastEnum.LLIKE)
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
    @QueryField(contrast = QueryContrastEnum.IN)
    @QueryMapping(value = "flag")
    private List<Integer> flagEquals;

    /**
     * 按创建时间范围查询（起始时间 >=）
     */
    @QueryMapping("create_time")
    @QueryField(contrast = QueryContrastEnum.GREATER_EQUAL)
    private Date createTimeStart;

    /**
     * 按创建时间范围查询（结束时间 <=）
     */
    @QueryMapping("create_time")
    @QueryField(contrast = QueryContrastEnum.LESS_EQUAL)
    private Date createTimeEnd;

    /**
     * 按更新时间范围查询（起始时间 >=）
     */
    @QueryMapping("update_time")
    @QueryField(contrast = QueryContrastEnum.GREATER_EQUAL)
    private Date updateTimeStart;

    /**
     * 按更新时间范围查询（结束时间 <=）
     */
    @QueryMapping("update_time")
    @QueryField(contrast = QueryContrastEnum.LESS_EQUAL)
    private Date updateTimeEnd;

    /**
     * 创建时间排序字段
     */
    @QueryOrderField(mappedName = "create_time", order = OrderConstant.ORDERED_MODEL)
    private QueryOrderedModel createTimeOrder;

    /**
     * 更新时间排序字段
     */
    @QueryOrderField(mappedName = "update_time", order = OrderConstant.ORDERED_MODEL)
    private QueryOrderedModel updateTimeOrder;

}
