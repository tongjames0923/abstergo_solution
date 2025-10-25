package com.tbs.shared.constant;

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
	 * 超级管理员角色实体
	 * <p>
	 * 包含角色ID、名称、描述等完整信息的系统角色对象
	 * </p>
	 */
	public static final SystemRoleEntity SUPER_ADMIN_ROLE = new SystemRoleEntity(SUPER_ADMIN_ROLE_ID, "超级管理员",
			"管理员功能都需要此权限", null, null, SystemRoleFlagEnum.SYSTEMED.getCode());

	/**
	 * 普通用户角色ID
	 */
	public static final long NORMAL_USER_ROLE_ID = 2L;

	/**
	 * 普通用户角色实体
	 * <p>
	 * 包含角色ID、名称、描述等完整信息的系统角色对象
	 * </p>
	 */
	public static final SystemRoleEntity NORMAL_USER_ROLE = new SystemRoleEntity(NORMAL_USER_ROLE_ID, "普通用户", "普通用户功能",
			null, null, SystemRoleFlagEnum.SYSTEMED.getCode());
}
