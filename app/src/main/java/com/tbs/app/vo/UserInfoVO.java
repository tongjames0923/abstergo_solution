package com.tbs.app.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

/**
 * 用户信息VO（View Object）
 * <p>
 * 用于向前端传递当前登录用户的基本信息，包括用户名、昵称、头像和可用角色列表
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Data
public class UserInfoVO implements Serializable {
	/**
	 * 用户名
	 */
	private String userName;

	/**
	 * 用户昵称
	 */
	private String nickName;

	/**
	 * 用户头像URL
	 */
	private String avatar;

	/**
	 * 用户可用角色列表
	 */
	private Collection<String> avaliableRoles;

	/**
	 * 用户可用部门列表
	 */
	private Collection<DepartmentInfoVO> avaliableDepartments;

	/**
	 * 用户当前选中的部门
	 */
	private DepartmentInfoVO currentDepartment;
}
