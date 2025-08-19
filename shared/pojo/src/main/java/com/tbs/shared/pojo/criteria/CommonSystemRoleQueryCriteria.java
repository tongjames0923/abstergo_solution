package com.tbs.shared.pojo.criteria;

import java.util.List;

import com.tbs.shared.pojo.entity.SystemRoleEntity;
import com.tbs.shared.pojo.model.ClassBaseQueryCriteria;
import com.tbs.sql.annotation.QueryField;
import com.tbs.sql.annotation.QueryMapping;
import com.tbs.sql.annotation.QueryOrderField;
import com.tbs.sql.constants.OrderConstant;
import com.tbs.sql.enums.QueryContrastEnum;
import com.tbs.sql.model.QueryOrderedModel;

import lombok.Data;

/**
 * 通用系统角色查询条件
 *
 * @author abstergo
 */
@Data
public class CommonSystemRoleQueryCriteria extends ClassBaseQueryCriteria<SystemRoleEntity> {
	@QueryMapping(value = "id")
	@QueryField(contrast = QueryContrastEnum.EQUAL)
	private Long idEquals;

	@QueryMapping(value = "role")
	@QueryField(contrast = QueryContrastEnum.EQUAL)
	private String roleEquals;

	@QueryMapping(value = "flag")
	@QueryField(contrast = QueryContrastEnum.IN)
	private List<Integer> flagEquals;

	@QueryOrderField(mappedName = "create_time", order = OrderConstant.ORDERED_MODEL)
	private QueryOrderedModel createTimeOrder;

	@QueryOrderField(mappedName = "update_time", order = OrderConstant.ORDERED_MODEL)
	private QueryOrderedModel updateTimeOrder;
}
