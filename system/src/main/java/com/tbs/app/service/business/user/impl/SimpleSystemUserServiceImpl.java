package com.tbs.app.service.business.user.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.business.role.IUserRoleRelationService;
import com.tbs.app.service.business.user.IPasswordEncrypt;
import com.tbs.app.service.business.user.ISystemUserService;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.EnumUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.enums.SystemUserStatusEnum;
import com.tbs.shared.interfaces.annoation.UserNameValid;
import com.tbs.shared.interfaces.constraint.IUserNamePasswordValidator;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.mapper.SystemUserMapper;
import com.tbs.shared.pojo.entity.SystemUserEntity;

/**
 * 简单的系统用户服务实现
 *
 * @author abstergo
 */
public class SimpleSystemUserServiceImpl
		extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemUserEntity, Long, SystemUserMapper>
		implements ISystemUserService {

	private static SimpleSystemUserServiceImpl instance;
	@Resource
	IUserNamePasswordValidator userNamePasswordValidator;
	@Resource
	SystemUserMapper mapper;
	@Resource
	private IPasswordEncrypt encrypt;
	@Resource
	@Lazy
	private IUserRoleRelationService relationService;
	@Resource
	private AppSettingProperty property;

	public SimpleSystemUserServiceImpl() {
		instance = this;
	}

	@Override
	protected Class<? extends IDelegateInvoker> pageVoBuildType() {
		return property.getMainDelegateInvokerImpl();
	}

	@Override
	protected SystemUserEntity insertNew(SystemUserEntity entity) {
		AssertUtil.isTrue(!userNamePasswordValidator.validateUserName(entity.getUserName()), "用户名格式错误");
		AssertUtil.isTrue(!userNamePasswordValidator.validatePassword(entity.getPassword()), "密码格式错误");
		AssertUtil.isTrue(!userNamePasswordValidator.validateNickName(entity.getNickName()), "昵称格式错误");
		entity.setPassword(encrypt.encrypt(entity.getPassword()));
		entity.setCreateTime(new Date());
		entity.setFlag(SystemUserStatusEnum.LOCKED.getCode());
		getMapper().insert(entity);
		relationService.enableRole(entity.getId(), CommonConstant.NORMAL_USER_ROLE_NAME);
		return entity;
	}

	@CacheEvict(value = "system_user", key = "#entity.id", condition = "#entity!=null&&#entity.id!=null")
	@Override
	protected List<SystemUserEntity> updateBy(SystemUserEntity entity) {
		entity.setUpdateTime(new Date());
		getMapper().updateByPrimaryKey(entity);
		return List.of(entity);
	}

	@Override
	protected SystemUserEntity compareAndUpdateValue(SystemUserEntity systemUserEntity,
			SystemUserEntity oldSystemUserEntity) {
		if (StrUtil.isNotEmpty(systemUserEntity.getUserName())) {
			AssertUtil.isTrue(!userNamePasswordValidator.validateUserName(systemUserEntity.getUserName()), "用户名格式错误");
			oldSystemUserEntity.setUserName(systemUserEntity.getUserName());
		}
		if (StrUtil.isNotEmpty(systemUserEntity.getPassword())) {
			AssertUtil.isTrue(!userNamePasswordValidator.validatePassword(systemUserEntity.getPassword()), "密码格式错误");
			oldSystemUserEntity.setPassword(encrypt.encrypt(systemUserEntity.getPassword()));
		}
		if (StrUtil.isNotEmpty(systemUserEntity.getNickName())) {
			AssertUtil.isTrue(!userNamePasswordValidator.validateNickName(systemUserEntity.getNickName()), "昵称格式错误");
			oldSystemUserEntity.setNickName(systemUserEntity.getNickName());
		}
		if (systemUserEntity.getFlag() != null) {
			AssertUtil.isTrue(!EnumUtil.isValidCode(SystemUserStatusEnum.class, systemUserEntity.getFlag()),
					"用户状态值异常，不存在{}值", systemUserEntity.getFlag());
			oldSystemUserEntity.setFlag(systemUserEntity.getFlag());
		}
		if (StrUtil.isNotEmpty(systemUserEntity.getAvatar())) {
			oldSystemUserEntity.setAvatar(systemUserEntity.getAvatar());
		}
		if (StrUtil.isNotEmpty(systemUserEntity.getRemark())) {
			oldSystemUserEntity.setRemark(systemUserEntity.getRemark());
		}
		oldSystemUserEntity.setUpdateTime(new Date());
		return oldSystemUserEntity;
	}

	@Override
	public List<SystemUserEntity> queryByUserName(@UserNameValid String username) {
		return getQueryWrapper().query().where().equal(SystemUserEntity::getUserName, username).pour().fetch();
	}

	@Cacheable(value = "system_user", key = "#id")
	@Override
	public SystemUserEntity queryById(Long id) {
		return super.queryById(id);
	}

	@Override
	protected SystemUserMapper getMapper() {
		return mapper;
	}

	@Override
	protected SystemUserEntity newEntity() {
		return new SystemUserEntity();
	}

	@Override
	protected Integer getFlagDisable() {
		return SystemUserStatusEnum.DISABLED.getCode();
	}
}
