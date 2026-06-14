package com.tbs.shared.pojo.criteria;

import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.shared.pojo.entity.SystemRoleEntity;
import com.tbs.shared.pojo.model.ClassBaseQueryCriteria;
import com.tbs.sql.annotation.condition.QueryField;
import com.tbs.sql.annotation.mapping.QueryMapping;
import com.tbs.sql.annotation.order.QueryOrderField;
import com.tbs.sql.constants.OrderConstants;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.order.impl.OrderedModel;
import lombok.Data;

import java.util.List;

/**
 * 通用系统角色查询条件 用于构建系统角色表(SystemRoleEntity)的查询条件
 *
 * @author abstergo
 */
@LogicDeleteEntity(activeFlagValues = {1, 2, 0}, filedName = "flag")
@Data
public class CommonSystemRoleQueryCriteria extends ClassBaseQueryCriteria<SystemRoleEntity> {
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
	@QueryField(contrast = ContrastEnum.EQUAL)
	private String roleEquals;

	/**
	 * 按标志位IN查询条件
	 */
	@QueryMapping(value = "flag")
	@QueryField(contrast = ContrastEnum.IN)
	private List<Integer> flagEquals;

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
}
