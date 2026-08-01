package com.tbs.shared.constant;

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
	 * 普通用户角色ID
	 */
	public static final long NORMAL_USER_ROLE_ID = 2L;

	/**
	 * 普通用户角色名称
	 */
	public static final String NORMAL_USER_ROLE_NAME = "普通用户";


	/**
	 * 开发者角色ID
	 */
	public static final long DEVELOPER_ROLE_ID = 3L;
	/**
	 * 开发者角色名称
	 */
	public static final String DEVELOPER_ROLE_NAME = "开发员";

}
