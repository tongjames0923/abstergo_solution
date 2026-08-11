package com.tbs.shared.pojo.criteria;

import com.tbs.shared.pojo.entity.SystemRoleGroupEntity;
import com.tbs.sql.annotation.condition.QueryField;
import com.tbs.sql.annotation.mapping.QueryMapping;
import com.tbs.sql.annotation.order.QueryOrderField;
import com.tbs.sql.constants.OrderConstants;
import com.tbs.sql.criteria.impl.DefaultEntityCriteria;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.order.impl.OrderedModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 系统角色组查询条件类
 * <p>
 * 用于构建系统角色组的查询条件，支持按名称模糊查询、描述模糊查询和状态列表查询
 * </p>
 * <p>
 * 继承自DefaultEntityCriteria，提供基础的实体查询能力
 * </p>
 *
 * @author abstergo
 */
@Data
public class CommonSystemRoleGroupCriteria extends DefaultEntityCriteria {

    /**
     * 角色组名称模糊查询条件
     * <p>
     * 映射到数据库字段role_group_name，使用左模糊匹配（LIKE '%value%'）
     * </p>
     */
    @QueryMapping("role_group_name")
    @QueryField(contrast = ContrastEnum.LLIKE)
    private String nameLike;

    /**
     * 角色组描述模糊查询条件
     * <p>
     * 映射到数据库字段description，使用左模糊匹配（LIKE '%value%'）
     * </p>
     */
    @QueryMapping("description")
    @QueryField(contrast = ContrastEnum.LLIKE)
    private String descriptionLike;

    /**
     * 角色组状态列表查询条件
     * <p>
     * 映射到数据库字段flag，使用IN查询匹配多个状态值
     * </p>
     */
    @QueryMapping("flag")
    @QueryField(contrast = ContrastEnum.IN)
    private List<Integer> flagIn;

    /**
     * 按ID精确查询条件
     * <p>
     * 映射到数据库字段id，使用精确匹配（=）
     * </p>
     */
    @QueryMapping("id")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Long idEquals;

    /**
     * 按ID集合IN查询条件
     * <p>
     * 映射到数据库字段id，使用IN查询匹配多个ID
     * </p>
     */
    @QueryMapping("id")
    @QueryField(contrast = ContrastEnum.IN)
    private List<Long> idIn;

    /**
     * 角色组名称精确查询条件
     * <p>
     * 映射到数据库字段role_group_name，使用精确匹配（=）
     * </p>
     */
    @QueryMapping("role_group_name")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String nameEquals;

    /**
     * 按创建者ID精确查询条件
     * <p>
     * 映射到数据库字段create_by，使用精确匹配（=）
     * </p>
     */
    @QueryMapping("create_by")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Long createByEquals;

    /**
     * 按更新者ID精确查询条件
     * <p>
     * 映射到数据库字段update_by，使用精确匹配（=）
     * </p>
     */
    @QueryMapping("update_by")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Long updateByEquals;

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
     * 无参构造函数
     * <p>
     * 初始化查询条件，指定查询的实体类型为SystemRoleGroupEntity
     * </p>
     */
    public CommonSystemRoleGroupCriteria() {
        super(SystemRoleGroupEntity.class);
    }
}
