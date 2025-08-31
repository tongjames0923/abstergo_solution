package com.tbs.app.service.business.department.impl;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.business.department.IDepartmentService;
import com.tbs.cache.annotation.ExpireOn;
import com.tbs.common.IStartup;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.mapper.SystemDepartmentMapper;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DefaultDepartmentService
		extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemDepartmentEntity, Long, SystemDepartmentMapper>
		implements IDepartmentService, IStartup {


	@Override
	public void startUp() throws RuntimeException {
		SystemDepartmentEntity entity = getMapper().selectOne(SystemDepartmentEntity.ROOT);
		if (entity == null) {
			log.info("系统部门不存在，默认插入...");
			getMapper().deleteByPrimaryKey(SystemDepartmentEntity.ROOT.getId());
			getMapper().insert(SystemDepartmentEntity.ROOT);
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

    @Caching(evict = {
			@CacheEvict(cacheNames = "department_children", allEntries = true) })
	@Override
	protected SystemDepartmentEntity insertNew(SystemDepartmentEntity entity) {
		entity.setFlag(CommonConstant.FLAG_ON);
		entity.setCreateTime(new java.util.Date());
		if (entity.getParentId() == null) {
			entity.setParentId(SystemDepartmentEntity.ROOT.getId());
		}
		var ins = (DefaultDepartmentService) instance;
		getMapper().insert(entity);
		return entity;
	}

	@Override
	@Caching(evict = { @CacheEvict(cacheNames = "department", key = "#entity.id"),
			@CacheEvict(cacheNames = "department_children", allEntries = true) })
	protected List<SystemDepartmentEntity> updateBy(SystemDepartmentEntity entity) {
		entity.setUpdateTime(new java.util.Date());

		getMapper().updateByPrimaryKey(entity);
		return List.of(entity);
	}

	@Override
	protected SystemDepartmentEntity compareAndUpdateValue(SystemDepartmentEntity newOne,
			SystemDepartmentEntity oldOne) {
		if (StrUtil.isNotEmpty(newOne.getDepartmentName())) {
			oldOne.setDepartmentName(newOne.getDepartmentName());
		}
		if (StrUtil.isNotEmpty(newOne.getDescription())) {
			oldOne.setDescription(newOne.getDescription());
		}
		if (newOne.getSortOrder() != null) {
			oldOne.setSortOrder(newOne.getSortOrder());
		}
		if (newOne.getFlag() != null) {
			oldOne.setFlag(newOne.getFlag());
		}
		if (newOne.getParentId() != null) {
			if (!newOne.getParentId().equals(oldOne.getParentId())) {
				oldOne.setParentId(newOne.getParentId());
			}
		}
		return oldOne;
	}

	@Caching(evict = { @CacheEvict(cacheNames = "department", key = "#id"),
			@CacheEvict(cacheNames = "department_children", allEntries = true) })
	@Override
	protected SystemDepartmentEntity handleDelete(Long id) {
		SystemDepartmentEntity department1 = getQueryWrapper().query().where()
				.equal(SystemDepartmentEntity::getParentId, id).pour().fetchOne();
		AssertUtil.isTrue(department1 != null, "请先删除子部门");
		return super.handleDelete(id);
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

    @Cacheable(cacheNames = "department_children", key = "#id")
	@ExpireOn(300)
	@Override
	public List<SystemDepartmentEntity> getAllChildren(Long id) {
		return queryWrapper.query().where().equal(SystemDepartmentEntity::getParentId, id).pour().fetch();
	}

}
