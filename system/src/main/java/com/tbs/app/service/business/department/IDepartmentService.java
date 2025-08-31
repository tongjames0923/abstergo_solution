package com.tbs.app.service.business.department;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;

import java.util.List;

/**
 * 部门服务
 *
 */
public interface IDepartmentService extends IServiceTemplate<SystemDepartmentEntity, Long> {


	/**
	 * 获取部门所有子部门
	 *
	 * @param id
	 * @return
	 */
	List<SystemDepartmentEntity> getAllChildren(Long id);

}
