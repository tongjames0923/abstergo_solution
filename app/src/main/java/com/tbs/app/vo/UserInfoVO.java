package com.tbs.app.vo;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.tbs.shared.pojo.entity.SystemDepartmentEntity;

import lombok.Data;

/**
 * 用户信息VO（View Object）
 * <p>
 * 用于向前端传递当前登录用户的基本信息，包括用户名、昵称、头像和可用角色列表
 * </p>
 *
 * @author tongj
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
	 * 部门信息
	 */
	private SystemDepartmentEntity department;

	/**
	 * 部门列表
	 */
	private List<SystemDepartmentEntity> departments;

	/**
	 * 用户可用角色列表
	 */
	private Collection<String> avaliableRoles;
}
