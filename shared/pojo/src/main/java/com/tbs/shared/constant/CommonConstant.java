package com.tbs.shared.constant;

import com.tbs.shared.enums.SystemRoleFlagEnum;
import com.tbs.shared.pojo.entity.SystemRoleEntity;

/**
 * 常用的常量值
 */
public class CommonConstant {
	public static final Integer FLAG_ON = 1;
	public static final Integer FLAG_OFF = 0;

	public static final long SUPER_ADMIN_ROLE_ID = 1L;
	public static final long NORMAL_USER_ROLE_ID = 2L;

	public static final SystemRoleEntity SUPER_ADMIN_ROLE = new SystemRoleEntity(SUPER_ADMIN_ROLE_ID, "超级管理员",
			"管理员功能都需要此权限", null, null, SystemRoleFlagEnum.SYSTEMED.getCode());
	public static final SystemRoleEntity NORMAL_USER_ROLE = new SystemRoleEntity(NORMAL_USER_ROLE_ID, "普通用户", "普通用户功能",
			null, null, SystemRoleFlagEnum.SYSTEMED.getCode());
}
