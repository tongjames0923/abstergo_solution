package com.tbs.app.service.business.department.impl;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.business.department.IDepartmentService;
import com.tbs.cache.annotation.ExpireOn;
import com.tbs.common.IStartup;
import com.tbs.common.data.ValueWrapper;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.mapper.DepartmentTreePathMapper;
import com.tbs.shared.mapper.SystemDepartmentMapper;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.shared.pojo.entity.SystemDepartmentTreePathEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
			entity.setParentId(SystemDepartmentEntity.ROOT.getId());
		}
		var ins = (DefaultDepartmentService)instance;
		getMapper().insert(entity);
		ins.buildTreePath(entity.getParentId());
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

	@Cacheable(cacheNames = "department", key = "#id")
	@ExpireOn(value = 15, timeUnit = TimeUnit.MINUTES)
	@Override
	public SystemDepartmentEntity queryById(Long id) {
		return super.queryById(id);
	}

	@Override
	@Cacheable(cacheNames = "departmentTreePath", key = "#parentId")
	@ExpireOn(value = 15, timeUnit = TimeUnit.MINUTES)
	public Optional<SystemDepartmentTreePathEntity> getTreePath(Long parentId) {
		return Optional.ofNullable(CollUtil.first(treePathMapper.queryByParentId(parentId)));
	}

	@Override
	@CacheEvict(cacheNames = "departmentTreePath", key = "#parentId")
	public SystemDepartmentTreePathEntity buildTreePath(Long parentId) {
		if (parentId == null) {
			parentId = SystemDepartmentEntity.NULL_PARENT_ID;
		}
		var ins = (DefaultDepartmentService)instance;
		ValueWrapper<SystemDepartmentTreePathEntity> grandParent = new ValueWrapper<>();
		if (parentId.equals(SystemDepartmentEntity.NULL_PARENT_ID)) {
			return SystemDepartmentTreePathEntity.ROOT_PATH;
		} else {
			SystemDepartmentEntity parent = ins.queryById(parentId);
			AssertUtil.notNull(parent, "父级部门ID{} 不存在", parentId);
			//获取父级部门路径
			var treePath = ins.getTreePath(parent.getParentId());

			treePath.ifPresentOrElse((p1) -> {
				//父级部门路径存在时
				grandParent.set(p1);
			}, () -> {
				//父级部门路径不存在时
				SystemDepartmentTreePathEntity p = ins.buildTreePath(parent.getParentId());
				grandParent.set(p);
			});
		}
		SystemDepartmentTreePathEntity p = grandParent.getOrThrow();

		SystemDepartmentTreePathEntity path = new SystemDepartmentTreePathEntity();
		path.setParentId(parentId);
		SystemDepartmentTreePathEntity oldOne = ins.getTreePath(parentId).orElse(null);
		String pathRaw = p.getTreePath() + p.getParentId() + "/";
		if (oldOne == null) {
			path.setTreePath(pathRaw);
			treePathMapper.insert(path);
		} else {
			if (!oldOne.getTreePath().equals(pathRaw)) {
				oldOne.setTreePath(pathRaw);
				treePathMapper.updateByPrimaryKey(oldOne);
			}
			path = oldOne;
		}
		return path;
	}
}
