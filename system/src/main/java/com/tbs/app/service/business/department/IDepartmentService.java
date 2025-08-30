package com.tbs.app.service.business.department;

import java.util.List;
import java.util.Optional;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.shared.pojo.entity.SystemDepartmentTreePathEntity;

/**
 * 部门服务
 *
 */
public interface IDepartmentService extends IServiceTemplate<SystemDepartmentEntity, Long> {
	/**
	 * 获取部门树路径
	 *
	 * @param parentId
	 * @return
	 */
	Optional<SystemDepartmentTreePathEntity> getTreePath(Long parentId);

	/**
	 * 获取部门所有父级部门
	 *
	 * @param id
	 * @return
	 */
	List<SystemDepartmentEntity> getAllParents(Long id);

	/**
	 * 构建部门树路径
	 *
	 * @param parentId
	 * @return
	 */
	SystemDepartmentTreePathEntity buildTreePath(Long parentId);

}
