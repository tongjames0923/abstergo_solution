package com.tbs.app.service.business.department.impl;

import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import com.tbs.app.service.business.department.IDepartmentService;
import com.tbs.shared.mapper.SystemDepartmentMapper;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.shared.pojo.model.PageVO;
import com.tbs.sql.query.IPage;
import com.tbs.sql.query.IQueryCriteria;

public class DefaultDepartmentService implements IDepartmentService {

	/**
	 * 部门数据访问接口
	 */
	@Resource
	private SystemDepartmentMapper systemDepartmentMapper;

	@Override
	public SystemDepartmentEntity deleteById(Long id) {
		return null;
	}

	@Override
	public int deleteByIds(Collection<Long> ids) {
		return 0;
	}

	@Override
	public List<SystemDepartmentEntity> save(SystemDepartmentEntity entity) {
		return List.of();
	}

	@Override
	public PageVO<SystemDepartmentEntity> queryBy(IQueryCriteria qo, IPage page) {
		return null;
	}

	@Override
	public SystemDepartmentEntity queryById(Long id) {
		return null;
	}
}
