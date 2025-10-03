package com.tbs.app.service.role;

import com.tbs.shared.pojo.entity.SystemRoleEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 用户角色关系服务接口
 *
 * @author tongj
 */
public interface IUserRoleRelationService {

	/**
	 * 启用角色
	 *
	 * @param userId
	 * @param roleId
	 */
	void enableRole(@NotNull Long userId, @NotNull Long roleId);

	/**
	 * 禁用角色
	 *
	 * @param userId
	 * @param roleId
	 */
	void disableRole(@NotNull Long userId, @NotNull Long roleId);

	/**
	 * 查询用户角色
	 *
	 * @param userId
	 * @return
	 */
	List<SystemRoleEntity> queryUserRoles(@NotNull Long userId);

	/**
	 * 查询用户角色名称,直接用于权限处理
	 *
	 * @param userId
	 * @return
	 */
	List<String> queryRoles(@NotNull Long userId);
}
