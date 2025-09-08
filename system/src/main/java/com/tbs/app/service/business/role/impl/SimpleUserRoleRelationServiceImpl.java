package com.tbs.app.service.business.role.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.tbs.web.uitl.AuthUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;

import com.tbs.app.service.business.role.ISystemRoleService;
import com.tbs.app.service.business.role.IUserRoleRelationService;
import com.tbs.app.service.business.user.ISystemUserService;
import com.tbs.cache.annotation.ExpireOn;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.enums.SystemRoleFlagEnum;
import com.tbs.shared.mapper.UserRoleRelationMapper;
import com.tbs.shared.pojo.entity.SystemRoleEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.shared.pojo.entity.UserRoleRelationEntity;
import com.tbs.sql.ef.IEntityQueryWrapper;

/**
 * @author tongj
 */
public class SimpleUserRoleRelationServiceImpl implements IUserRoleRelationService {

	@Lazy
	@Resource
	private ISystemUserService systemUserService;

	@Resource
	@Lazy
	private ISystemRoleService systemRoleService;

	@Resource
	private IEntityQueryWrapper<UserRoleRelationEntity> wrapper;

	@Resource
	private UserRoleRelationMapper userRoleRelationMapper;

	@PostConstruct
	void init() {
		wrapper.makeQueryable(new UserRoleRelationEntity());
	}

	@Override
	@CacheEvict(cacheNames = "user_role", key = "#userId")
	public void enableRole(@NotNull Long userId, @NotNull Long role) {
		setRole(userId, role, SystemRoleFlagEnum.ACTIVE.getCode());
	}

	private void setRole(@NotNull Long userId, @NotNull Long role, @NotNull Integer flag) {
		AssertUtil.notNull(role, "角色不能为空");
		SystemUserEntity user = systemUserService.queryById(userId);
		AssertUtil.notNull(user, "用户不存在");
		SystemRoleEntity roleEntity = systemRoleService.queryById(role);
		AssertUtil.notNull(roleEntity, "角色不存在");
		AssertUtil.isTrue(SystemRoleFlagEnum.DISABLE.getCode().equals(roleEntity.getFlag()), "角色 {} 已失效", role);
		UserRoleRelationEntity relation = wrapper.query().where().equal(UserRoleRelationEntity::getUserId, userId)
				.equal(UserRoleRelationEntity::getRoleId, roleEntity.getId()).pour().fetchOne();
		if (relation == null) {
			relation = new UserRoleRelationEntity();
			relation.setUserId(userId);
			relation.setRoleId(roleEntity.getId());
			relation.setFlag(flag);
			relation.setCreateTime(new Date());
			userRoleRelationMapper.insert(relation);
		} else {
			relation.setFlag(flag);
			relation.setUpdateTime(new Date());
			userRoleRelationMapper.updateByPrimaryKey(relation);
		}
        AuthUtil.getInstance().notifyUserAuthStatusChanged(user.getUserName());
	}

	@Override
	@CacheEvict(cacheNames = "user_role", key = "#userId")
	public void disableRole(@NotNull Long userId, @NotNull Long roleId) {
		setRole(userId, roleId, SystemRoleFlagEnum.DISABLE.getCode());
	}

	@Override
	public List<SystemRoleEntity> queryUserRoles(@NotNull Long userId) {
		List<UserRoleRelationEntity> relations = wrapper.query().where()
				.equal(UserRoleRelationEntity::getUserId, userId)
				.equal(UserRoleRelationEntity::getFlag, CommonConstant.FLAG_ON).pour().fetch();
		if (CollUtil.isEmpty(relations)) {
			return Arrays.asList();
		}
		return relations.stream().map(relation -> systemRoleService.queryById(relation.getRoleId()))
				.filter(role -> role != null && !SystemRoleFlagEnum.DISABLE.getCode().equals(role.getFlag())).distinct()
				.collect(Collectors.toList());
	}

	@Cacheable(cacheNames = "user_role", key = "#userId")
	@ExpireOn(cacheNames = "user_role", value = 15, timeUnit = TimeUnit.MINUTES)
	@Override
	public List<String> queryRoles(@NotNull Long userId) {
		return wrapper.query().where().equal(UserRoleRelationEntity::getUserId, userId)
				.equal(UserRoleRelationEntity::getFlag, CommonConstant.FLAG_ON).pour().fetch().stream()
				.map(relation -> systemRoleService.queryById(relation.getRoleId()))
				.filter(role -> role != null && role.getId() != null
						&& !SystemRoleFlagEnum.DISABLE.getCode().equals(role.getFlag()))
				.map(role -> role.getId()).map(roleId -> roleId.toString()).collect(Collectors.toList());
	}
}
