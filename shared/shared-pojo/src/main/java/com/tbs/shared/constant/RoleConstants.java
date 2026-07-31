package com.tbs.shared.constant;

import com.tbs.shared.enums.SystemDataScopeEnum;
import com.tbs.shared.enums.SystemRoleFlagEnum;
import com.tbs.shared.pojo.entity.SystemRoleEntity;

/**
 * 系统角色常量类
 * <p>
 * 该类定义了系统中使用的标准角色常量，包括角色ID和角色实体对象。 目前包含超级管理员和普通用户两种系统预定义角色。
 * </p>
 */
public class RoleConstants {

	/**
	 * 超级管理员角色ID
	 */
	public static final long SUPER_ADMIN_ROLE_ID = 1L;
	/**
	 * 超级管理员角色名称
	 */
	public static final String SUPER_ADMIN_ROLE_NAME = "超级管理员";

	/**
	 * 超级管理员角色实体
	 * <p>
	 * 包含角色ID、名称、描述等完整信息的系统角色对象
	 * </p>
	 */
	public static final SystemRoleEntity SUPER_ADMIN_ROLE =
		new SystemRoleEntity(SUPER_ADMIN_ROLE_ID, SUPER_ADMIN_ROLE_NAME, "开放数据后台管理和系统日志查看功能。", null,
			null, SystemRoleFlagEnum.SYSTEMED.getCode(), null, null, SystemDataScopeEnum.ALL.getCode());

	/**
	 * 普通用户角色ID
	 */
	public static final long NORMAL_USER_ROLE_ID = 2L;

	/**
	 * 普通用户角色名称
	 */
	public static final String NORMAL_USER_ROLE_NAME = "普通用户";

	/**
	 * 普通用户角色实体
	 * <p>
	 * 包含角色ID、名称、描述等完整信息的系统角色对象
	 * </p>
	 */
	public static final SystemRoleEntity NORMAL_USER_ROLE =
		new SystemRoleEntity(NORMAL_USER_ROLE_ID, NORMAL_USER_ROLE_NAME, "基础登录权限，若无此权限，用户将无法登录", null,
			null, SystemRoleFlagEnum.SYSTEMED.getCode(), null, null, SystemDataScopeEnum.SELF.getCode());

	/**
	 * 开发者角色ID
	 */
	public static final long DEVELOPER_ROLE_ID = 3L;
	/**
	 * 开发者角色名称
	 */
	public static final String DEVELOPER_ROLE_NAME = "开发员";
	/**
	 * 开发者角色实体
	 * <p>
	 * 包含角色ID、名称、描述等完整信息的系统角色对象
	 * </p>
	 */
	public static final SystemRoleEntity DEVELOPER_ROLE =
		new SystemRoleEntity(DEVELOPER_ROLE_ID, DEVELOPER_ROLE_NAME, "开放表单管理和日志功能。", null, null,
			SystemRoleFlagEnum.SYSTEMED.getCode(), null, null, SystemDataScopeEnum.ALL.getCode());
}
