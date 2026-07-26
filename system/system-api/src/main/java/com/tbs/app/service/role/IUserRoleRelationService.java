package com.tbs.app.service.role;

import com.tbs.shared.pojo.entity.SystemRoleEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
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
	void enableRole( Long userId,  Long roleId);

	/**
	 * 禁用角色
	 *
	 * @param userId
	 * @param roleId
	 */
	void disableRole( Long userId,  Long roleId);

	/**
	 * 查询用户角色
	 *
	 * @param userId
	 * @return
	 */
	List<SystemRoleEntity> queryUserRoles( Long userId);

	/**
	 * 查询用户角色名称,直接用于权限处理
	 *
	 * @param userId
	 * @return
	 */
	List<String> queryRoles( Long userId);

	/**
	 * 查询角色用户
	 *
	 * @param roleId
	 * @return
	 */
	List<SystemUserEntity> queryUsersByRole( Long roleId);

	/**
	 * 判断角色是否被使用
	 *
	 * @param roleId
	 * @return
	 */
	boolean isRoleInUsed( Long roleId);

}
