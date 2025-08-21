package com.tbs.app.service.business.department.impl;

import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.business.department.IDepartmentService;
import com.tbs.common.IStartup;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.util.CollUtil;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.mapper.DepartmentTreePathMapper;
import com.tbs.shared.mapper.SystemDepartmentMapper;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.shared.pojo.entity.SystemDepartmentTreePathEntity;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DefaultDepartmentService
		extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemDepartmentEntity, Long, SystemDepartmentMapper>
		implements IDepartmentService, IStartup {

	@Resource
	DepartmentTreePathMapper treePathMapper;

	@Override
	public void startUp() throws RuntimeException {
		SystemDepartmentEntity entity = getMapper().selectOne(SystemDepartmentEntity.ROOT);
		if (entity == null) {
			log.info("系统部门不存在，默认插入...");
			getMapper().deleteByPrimaryKey(SystemDepartmentEntity.ROOT.getId());
			getMapper().insert(SystemDepartmentEntity.ROOT);
			treePathMapper.deleteByPrimaryKey(SystemDepartmentTreePathEntity.ROOT_PATH.getId());
			treePathMapper.insert(SystemDepartmentTreePathEntity.ROOT_PATH);
		}
	}

	@Override
	protected Integer getFlagDisable() {
		return CommonConstant.FLAG_OFF;
	}

	@Resource
	private AppSettingProperty property;

	@Override
	protected Class<? extends IDelegateInvoker> pageVoBuildType() {
		return property.getMainDelegateInvokerImpl();
	}

	@Override
	protected SystemDepartmentEntity insertNew(SystemDepartmentEntity entity) {
		entity.setFlag(CommonConstant.FLAG_ON);
		entity.setCreateTime(new java.util.Date());
		if (entity.getParentId() == null) {
			entity.setParentId(SystemDepartmentEntity.ROOT.getParentId());
		}

		getMapper().insert(entity);
		return entity;
	}

	@Override
	protected List<SystemDepartmentEntity> updateBy(SystemDepartmentEntity entity) {
		return List.of();
	}

	@Override
	protected SystemDepartmentEntity compareAndUpdateValue(SystemDepartmentEntity newOne,
			SystemDepartmentEntity oldOne) {
		return null;
	}

	@Resource
	private SystemDepartmentMapper departmentMapper;

	@Override
	protected SystemDepartmentMapper getMapper() {
		return departmentMapper;
	}

	private SystemDepartmentEntity department = new SystemDepartmentEntity();

	@Override
	protected SystemDepartmentEntity newEntity() {
		return department;
	}

	@Override
	public Optional<SystemDepartmentTreePathEntity> getTreePath(Long parentId) {
		return Optional.ofNullable(CollUtil.first(treePathMapper.queryByParentId(parentId)));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public SystemDepartmentTreePathEntity buildTreePath(Long parentId) {
//		if (parentId == null) {
//			parentId = SystemDepartmentEntity.NULL_PARENT_ID;
//		}
//		var treePaths = getTreePath(parentId);
//		if (treePaths.isPresent()) {
//			treePathMapper.deleteByPrimaryKey(treePaths.get().getId());
//		}

		// FIXME 临时处理
		return null;
	}
}
