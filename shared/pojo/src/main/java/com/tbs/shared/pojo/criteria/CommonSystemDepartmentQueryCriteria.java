package com.tbs.shared.pojo.criteria;

import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.shared.pojo.model.ClassBaseQueryCriteria;
import com.tbs.sql.annotation.QueryField;
import com.tbs.sql.annotation.QueryMapping;
import com.tbs.sql.enums.QueryContrastEnum;

import lombok.Data;

@Data
public class CommonSystemDepartmentQueryCriteria extends ClassBaseQueryCriteria<SystemDepartmentEntity> {

	/**
	 * 部门ID
	 */
	@QueryMapping(value = "id")
	@QueryField(contrast = QueryContrastEnum.EQUAL)
	private Long idEquals;
	/**
	 * 父部门ID
	 */
	@QueryMapping(value = "parent_id")
	@QueryField(contrast = QueryContrastEnum.EQUAL)
	private Long parentIdEquals;

	/**
	 * 部门名称
	 */
	@QueryMapping(value = "department_name")
	@QueryField(contrast = QueryContrastEnum.LLIKE)
	private String departmentNameLike;

}
