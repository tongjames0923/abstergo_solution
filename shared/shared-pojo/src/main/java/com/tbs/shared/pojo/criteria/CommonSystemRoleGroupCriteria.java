package com.tbs.shared.pojo.criteria;

import com.tbs.shared.pojo.entity.SystemRoleGroupEntity;
import com.tbs.sql.annotation.condition.QueryField;
import com.tbs.sql.annotation.mapping.QueryMapping;
import com.tbs.sql.criteria.impl.DefaultEntityCriteria;
import com.tbs.sql.enums.ContrastEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 无参构造函数
     * <p>
     * 初始化查询条件，指定查询的实体类型为SystemRoleGroupEntity
     * </p>
     */
    public CommonSystemRoleGroupCriteria() {
        super(SystemRoleGroupEntity.class);
    }
}
