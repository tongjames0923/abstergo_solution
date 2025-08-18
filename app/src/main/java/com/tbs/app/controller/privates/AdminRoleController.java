package com.tbs.app.controller.privates;

import javax.annotation.Resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tbs.app.service.business.role.ISystemRoleService;
import com.tbs.app.service.business.role.IUserRoleRelationService;
import com.tbs.app.service.business.user.ISystemUserService;
import com.tbs.common.error.AssertUtil;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.enums.SystemUserStatusEnum;
import com.tbs.shared.pojo.criteria.CommonSystemRoleQueryCriteria;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.sql.model.QueryPageModel;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 管理员角色管理控制器
 * <p>
 * 该控制器用于管理管理员角色相关的操作，包括查询用户角色、启用/禁用角色、激活/禁用账户等操作
 * </p>
 *
 * @author abstergo
 */
@RequestMapping("/private/admin")
@RestController
@PreAuthorize("hasAuthority('" + CommonConstant.SUPER_ADMIN_ROLE_NAME + "')")
@Api(value = "管理员角色管理", tags = "管理员角色管理接口")
public class AdminRoleController extends BaseController {

	@Resource
	private IUserRoleRelationService relationService;

	@Resource
	private ISystemRoleService roleService;

	/**
	 * 查询用户角色列表
	 *
	 * @param userId 用户ID
	 * @return 用户角色列表
	 */
	@GetMapping("roles/{userId}")
	@ApiOperation(value = "查询用户角色", notes = "根据用户ID查询该用户拥有的所有角色")
	public AjaxResult userRoles(@ApiParam(name = "userId", value = "用户ID", required = true) @PathVariable Long userId) {
		return successResult(relationService.queryUserRoles(userId));
	}

	/**
	 * 启用用户角色
	 *
	 * @param userId 用户ID
	 * @param role   角色名称
	 * @return 操作结果
	 */
	@GetMapping("/role/enable/{userId}")
	@ApiOperation(value = "启用用户角色", notes = "为指定用户启用指定角色")
	public AjaxResult enable(@ApiParam(name = "userId", value = "用户ID", required = true) @PathVariable Long userId,
			@ApiParam(name = "role", value = "角色名称", required = true) @RequestParam String role) {
		relationService.enableRole(userId, role);
		return AjaxUtil.getInstance().success("授权成功", null);
	}

	/**
	 * 查询角色列表
	 *
	 * @param criteria 查询条件
	 * @param page     页码
	 * @param num      每页数量
	 * @return 角色列表
	 */
	@PostMapping("roles/query")
	@ApiOperation(value = "查询角色列表", notes = "根据条件查询角色列表，支持分页")
	public AjaxResult query(
			@ApiParam(name = "criteria", value = "查询条件") @RequestBody CommonSystemRoleQueryCriteria criteria,
			@ApiParam(name = "page", value = "页码") @RequestParam(required = false) Long page,
			@ApiParam(name = "num", value = "每页数量") @RequestParam(required = false) Long num) {
		return successResult(roleService.queryBy(criteria, QueryPageModel.of(page, num)));
	}

	/**
	 * 禁用用户角色
	 *
	 * @param userId 用户ID
	 * @param role   角色名称
	 * @return 操作结果
	 */
	@GetMapping("/role/disable/{userId}")
	@ApiOperation(value = "禁用用户角色", notes = "为指定用户禁用指定角色")
	public AjaxResult disable(@ApiParam(name = "userId", value = "用户ID", required = true) @PathVariable Long userId,
			@ApiParam(name = "role", value = "角色名称", required = true) @RequestParam String role) {
		relationService.disableRole(userId, role);
		return AjaxUtil.getInstance().success("取消授权成功", null);
	}

	@Resource
	private ISystemUserService userService;

	/**
	 * 激活用户账户
	 *
	 * @param userId 用户ID
	 * @return 操作结果
	 */
	@GetMapping("/account/activite/{userId}")
	@ApiOperation(value = "激活用户账户", notes = "激活指定用户的账户")
	public AjaxResult activiteAccount(
			@ApiParam(name = "userId", value = "用户ID", required = true) @PathVariable Long userId) {
		SystemUserEntity user = userService.queryById(userId);
		AssertUtil.notNull(user, "用户不存在");
		user.setFlag(SystemUserStatusEnum.ACTIVITE.getCode());
		userService.save(user);
		relationService.enableRole(userId, CommonConstant.NORMAL_USER_ROLE_NAME);
		return AjaxUtil.getInstance().success("激活成功", null);
	}

	/**
	 * 禁用用户账户
	 *
	 * @param userId 用户ID
	 * @return 操作结果
	 */
	@GetMapping("/account/disable/{userId}")
	@ApiOperation(value = "禁用用户账户", notes = "禁用指定用户的账户")
	public AjaxResult disableAccount(
			@ApiParam(name = "userId", value = "用户ID", required = true) @PathVariable Long userId) {
		SystemUserEntity user = userService.queryById(userId);
		AssertUtil.notNull(user, "用户不存在");
		user.setFlag(SystemUserStatusEnum.DISABLED.getCode());
		userService.save(user);
		return AjaxUtil.getInstance().success("禁用成功", null);
	}

	@Override
	public String getBusinessName() {
		return "管理员角色管理";
	}
}
