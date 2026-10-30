package com.tbs.shared.pojo.criteria;

import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.shared.pojo.entity.SystemRoleEntity;
import com.tbs.sql.annotation.condition.QueryField;
import com.tbs.sql.annotation.mapping.QueryMapping;
import com.tbs.sql.annotation.order.QueryOrderField;
import com.tbs.sql.constants.OrderConstants;
import com.tbs.sql.criteria.impl.DefaultEntityCriteria;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.order.impl.OrderedModel;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 通用系统角色查询条件 用于构建系统角色表(SystemRoleEntity)的查询条件
 *
 * @author abstergo
 */
@LogicDeleteEntity(activeFlagValues = {1, 2, 0}, filedName = "flag")
@Data
public class CommonSystemRoleQueryCriteria extends DefaultEntityCriteria {
	/**
	 * 按ID精确查询条件
	 */
	@QueryMapping(value = "id")
	@QueryField(contrast = ContrastEnum.EQUAL)
	private Long idEquals;

	/**
	 * 按角色名精确查询条件
	 */
	@QueryMapping(value = "role")
	@QueryField(contrast = ContrastEnum.LLIKE)
	private String roleEquals;

	/**
	 * 按角色名模糊查询条件
	 */
	@QueryMapping(value = "name")
	@QueryField(contrast = ContrastEnum.LLIKE)
	private String nameLike;

	/**
	 * 按角色类型精确查询条件
	 */
	@QueryMapping(value = "type")
	@QueryField(contrast = ContrastEnum.IN)
	private List<Integer> typeEquals;

	/**
	 * 按标志位IN查询条件
	 */
	@QueryMapping(value = "flag")
	@QueryField(contrast = ContrastEnum.IN)
	private List<Integer> flagEquals;

	/**
	 * 按ID集合IN查询条件
	 */
	@QueryMapping(value = "id")
	@QueryField(contrast = ContrastEnum.IN)
	private List<Long> idIn;

	/**
	 * 按权限编码集合IN查询条件
	 */
	@QueryMapping(value = "role")
	@QueryField(contrast = ContrastEnum.IN)
	private List<String> roleIn;

	/**
	 * 按名称精确查询条件
	 */
	@QueryMapping(value = "name")
	@QueryField(contrast = ContrastEnum.EQUAL)
	private String nameEquals;

	/**
	 * 按类型精确查询条件
	 */
	@QueryMapping(value = "type")
	@QueryField(contrast = ContrastEnum.EQUAL)
	private Integer typeEqual;

	/**
	 * 按描述模糊查询条件
	 */
	@QueryMapping(value = "description")
	@QueryField(contrast = ContrastEnum.LLIKE)
	private String descriptionLike;

	/**
	 * 按数据权限精确查询条件
	 */
	@QueryMapping(value = "data_scope")
	@QueryField(contrast = ContrastEnum.EQUAL)
	private Integer dataScopeEquals;

	/**
	 * 按数据权限集合IN查询条件
	 */
	@QueryMapping(value = "data_scope")
	@QueryField(contrast = ContrastEnum.IN)
	private List<Integer> dataScopeIn;

	/**
	 * 按创建者ID精确查询条件
	 */
	@QueryMapping(value = "create_by")
	@QueryField(contrast = ContrastEnum.EQUAL)
	private Long createByEquals;

	/**
	 * 按更新者ID精确查询条件
	 */
	@QueryMapping(value = "update_by")
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
	 * 按创建时间排序条件
	 */
	@QueryOrderField(mappedName = "create_time", order = OrderConstants.ORDERED_MODEL)
	private OrderedModel createTimeOrder;

	/**
	 * 按更新时间排序条件
	 */
	@QueryOrderField(mappedName = "update_time", order = OrderConstants.ORDERED_MODEL)
	private OrderedModel updateTimeOrder;

	public CommonSystemRoleQueryCriteria() {
		super(SystemRoleEntity.class);
	}
}
