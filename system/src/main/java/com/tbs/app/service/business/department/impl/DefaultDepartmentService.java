package com.tbs.app.service.business.department.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.business.department.IDepartmentService;
import com.tbs.cache.annotation.ExpireOn;
import com.tbs.common.IStartup;
import com.tbs.common.data.ValueWrapper;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.common.util.StrUtil;
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

	@Caching(evict = { @CacheEvict(cacheNames = "departmentTreePath", allEntries = true),
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
		ins.buildTreePath(entity.getParentId());
		return entity;
	}

	@Override
	@Caching(evict = { @CacheEvict(cacheNames = "department", key = "#entity.id"),
			@CacheEvict(cacheNames = "departmentTreePath", allEntries = true),
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
				((DefaultDepartmentService) instance).buildTreePath(oldOne.getParentId());
			}
		}
		return oldOne;
	}

	@Caching(evict = { @CacheEvict(cacheNames = "department", key = "#id"),
			@CacheEvict(cacheNames = "departmentTreePath", allEntries = true),
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

	@Override
	@Cacheable(cacheNames = "departmentTreePath", key = "#parentId")
	@ExpireOn(value = 15, timeUnit = TimeUnit.MINUTES)
	public Optional<SystemDepartmentTreePathEntity> getTreePath(Long parentId) {
		return Optional.ofNullable(CollUtil.first(treePathMapper.queryByParentId(parentId)));
	}

	@Override
	public List<SystemDepartmentEntity> getAllParents(Long id) {
		// 如果id为null或为根部门ID，则返回空列表
		if (id == null || id.equals(SystemDepartmentEntity.ROOT.getId())
				|| id.equals(SystemDepartmentEntity.NULL_PARENT_ID)) {
			return List.of();
		}

		// 查询当前部门
		SystemDepartmentEntity department = instance.queryById(id);
		if (department == null || department.getParentId() == null
				|| department.getParentId().equals(SystemDepartmentEntity.NULL_PARENT_ID)) {
			return List.of();
		}

		// 获取当前部门的树路径
		Optional<SystemDepartmentTreePathEntity> treePathOpt = ((IDepartmentService) instance)
				.getTreePath(department.getParentId());
		if (treePathOpt.isEmpty()) {
			// 如果没有树路径，手动递归查询父级部门
			List<SystemDepartmentEntity> parents = new ArrayList<>();
			Long parentId = department.getParentId();

			while (parentId != null && !parentId.equals(SystemDepartmentEntity.NULL_PARENT_ID)) {
				SystemDepartmentEntity parent = instance.queryById(parentId);
				if (parent == null) {
					break;
				}
				parents.add(parent);
				parentId = parent.getParentId();
			}

			return parents;
		}

		// 从树路径中提取所有父级部门ID
		SystemDepartmentTreePathEntity treePath = treePathOpt.get();
		String path = treePath.getTreePath();

		// 路径格式为 "/1/2/3/"，需要解析出各级父部门ID
		String[] parts = path.split("/");
		List<Long> parentIds = new ArrayList<>();

		for (String part : parts) {
			if (!part.isEmpty()) {
				try {
					parentIds.add(Long.parseLong(part));
				} catch (NumberFormatException ignored) {
					// 忽略无效的ID
				}
			}
		}

		// 添加直接父级部门ID
		parentIds.add(treePath.getParentId());

		// 根据ID列表查询所有父级部门
		if (parentIds.isEmpty()) {
			return List.of();
		}

		// 使用BaseMapper的selectByIds方法查询所有父级部门
		List<SystemDepartmentEntity> parents = new ArrayList<>();
		for (Long parentId : parentIds) {
			if (!parentId.equals(SystemDepartmentEntity.NULL_PARENT_ID)) {
				SystemDepartmentEntity parent = instance.queryById(parentId);
				if (parent != null) {
					parents.add(parent);
				}
			}
		}

		return parents;
	}

	@Override
	@CacheEvict(cacheNames = "departmentTreePath", key = "#parentId")
	public SystemDepartmentTreePathEntity buildTreePath(Long parentId) {
		if (parentId == null) {
			parentId = SystemDepartmentEntity.NULL_PARENT_ID;
		}
		var ins = (DefaultDepartmentService) instance;
		ValueWrapper<SystemDepartmentTreePathEntity> grandParent = new ValueWrapper<>();
		if (parentId.equals(SystemDepartmentEntity.NULL_PARENT_ID)) {
			return SystemDepartmentTreePathEntity.ROOT_PATH;
		} else {
			SystemDepartmentEntity parent = ins.queryById(parentId);
			AssertUtil.notNull(parent, "父级部门ID{} 不存在", parentId);
			// 获取父级部门路径
			var treePath = ins.getTreePath(parent.getParentId());

			treePath.ifPresentOrElse((p1) -> {
				// 父级部门路径存在时
				grandParent.set(p1);
			}, () -> {
				// 父级部门路径不存在时
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

	@Cacheable(cacheNames = "department_children", key = "#id")
	@ExpireOn(300)
	@Override
	public List<SystemDepartmentEntity> getAllChildren(Long id) {
		return queryWrapper.query().where().equal(SystemDepartmentEntity::getParentId, id).pour().fetch();
	}

}
