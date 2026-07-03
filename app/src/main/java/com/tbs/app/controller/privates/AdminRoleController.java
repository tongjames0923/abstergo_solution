package com.tbs.app.controller.privates;

import com.tbs.app.service.role.ISystemRoleService;
import com.tbs.app.service.role.IUserRoleRelationService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.shared.constant.RoleConstants;
import com.tbs.shared.enums.SystemUserStatusEnum;
import com.tbs.shared.pojo.criteria.CommonSystemRoleQueryCriteria;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.sql.model.query.QueryPageModel;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.shared.pojo.model.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 管理员角色管理控制器,用于管理用户使用的角色权限。
 * <p>
 * 该控制器用于控制用户与权限的绑定关系
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RequestMapping("/private/admin/roles")
@RestController
@PreAuthorize("hasAuthority('" + RoleConstants.SUPER_ADMIN_ROLE_NAME + "')")
@Tag(name = "用户角色管理接口", description = "用户角色管理接口")
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
    @GetMapping("/{userId}")
    @AjaxResponseType(value = List.class)
    @Operation(summary = "查询用户角色", description = "根据用户ID查询该用户拥有的所有角色")
    @ApiResponse(responseCode = "200", description = "成功查询用户角色",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult userRoles(
        @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable Long userId) {
        return successResult(relationService.queryUserRoles(userId));
    }

    /**
     * 启用用户角色
     *
     * @param userId 用户ID
     * @param role   角色名称
     * @return 操作结果
     */
    @GetMapping("/enable/{userId}")
    @AjaxResponseType(void.class)
    @Operation(summary = "启用用户角色", description = "为指定用户启用指定角色")
    @ApiResponse(responseCode = "200", description = "成功启用用户角色",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult enable(
        @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable Long userId,
        @Parameter(name = "role", description = "角色名称", required = true) @RequestParam Long role) {
        relationService.enableRole(userId, role);
        return AjaxUtil.getInstance().success("授权成功", null);
    }

    /**
     * 禁用用户角色
     *
     * @param userId 用户ID
     * @param role   角色名称
     * @return 操作结果
     */
    @GetMapping("/disable/{userId}")
    @AjaxResponseType(void.class)
    @Operation(summary = "禁用用户角色", description = "为指定用户禁用指定角色")
    @ApiResponse(responseCode = "200", description = "成功禁用用户角色",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult disable(
        @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable Long userId,
        @Parameter(name = "role", description = "角色名称", required = true) @RequestParam Long role) {
        relationService.disableRole(userId, role);
        return AjaxUtil.getInstance().success("取消授权成功", null);
    }

    /**
     * 查询角色列表
     *
     * @param criteria 查询条件
     * @param page     页码
     * @param num      每页数量
     * @return 角色列表
     */
    @PostMapping("/query")
    @AjaxResponseType(PageVO.class)
    @Operation(summary = "查询角色列表", description = "根据条件查询角色列表，支持分页")
    @ApiResponse(responseCode = "200", description = "成功查询角色列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult query(
        @Parameter(name = "criteria", description = "查询条件") @RequestBody CommonSystemRoleQueryCriteria criteria,
        @Parameter(name = "page", description = "页码") @RequestParam(required = false) Long page,
        @Parameter(name = "num", description = "每页数量") @RequestParam(required = false) Long num) {
        return successResult(roleService.queryAndPage(criteria, QueryPageModel.of(page, num)));
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
    @AjaxResponseType(void.class)
    @Operation(summary = "激活用户账户", description = "激活指定用户的账户")
    @ApiResponse(responseCode = "200", description = "成功激活用户账户",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult activiteAccount(
        @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable Long userId) {
        SystemUserEntity user = userService.queryById(userId);
        AssertUtil.isTrue(user == null).message("用户不存在").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        user.setFlag(SystemUserStatusEnum.ACTIVITE.getCode());
        userService.save(user);
        relationService.enableRole(userId, RoleConstants.NORMAL_USER_ROLE_ID);
        return AjaxUtil.getInstance().success("激活成功", null);
    }

    /**
     * 禁用用户账户
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @GetMapping("/account/disable/{userId}")
    @AjaxResponseType(void.class)
    @Operation(summary = "禁用用户账户", description = "禁用指定用户的账户")
    @ApiResponse(responseCode = "200", description = "成功禁用用户账户",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult disableAccount(
        @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable Long userId) {
        SystemUserEntity user = userService.queryById(userId);
        AssertUtil.isTrue(user == null).message("用户不存在").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        user.setFlag(SystemUserStatusEnum.DISABLED.getCode());
        userService.save(user);
        relationService.disableRole(userId, RoleConstants.NORMAL_USER_ROLE_ID);
        return AjaxUtil.getInstance().success("禁用成功", null);
    }

    @Override
    public String getBusinessName() {
        return "管理员角色管理";
    }
}
