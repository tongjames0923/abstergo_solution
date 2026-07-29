package com.tbs.app.controller.privates;


import com.tbs.app.service.department.IDepartmentUserRelationService;
import com.tbs.app.vo.DepartmentInfoVO;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 私有部门控制器
 * <p>
 * 提供需要登录才能访问的部门相关接口，包括获取用户可访问的部门列表
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/private/department")
@PreAuthorize("@authUtil.userLoggedIn")
@Tag(name = "私有部门管理接口", description = "需要登录才能访问的部门相关接口")
public class PrivateDepartmentController extends BaseController {

    @Resource
    private IDepartmentUserRelationService relationService;

    /**
     * 获取用户可访问部门列表
     * <p>
     * 根据用户ID查询该用户可以访问的所有部门信息
     * </p>
     *
     * @param userId 用户ID
     * @return 用户可访问的部门列表
     */
    @GetMapping("avaliable/{userId}")
    @AjaxResponseType(value = java.util.List.class, genericArgs = {DepartmentInfoVO.class})
    @Operation(summary = "获取用户可访问部门列表", description = "根据用户ID查询该用户可以访问的所有部门信息")
    @ApiResponse(responseCode = "200", description = "成功获取用户可访问部门列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult avaliable(
        @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable Long userId) {
        return AjaxUtil.getInstance().success(relationService.queryUserDepartments(userId));
    }

    /**
     * 将用户添加到指定部门
     * <p>
     * 根据部门ID和用户ID建立用户与部门的关联关系
     * </p>
     *
     * @param depId  部门ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("addEmp/{depId}/{userId}")
    @AjaxResponseType(void.class)
    @Operation(summary = "添加用户到部门", description = "将指定用户添加到指定部门")
    @ApiResponse(responseCode = "200", description = "成功添加用户到部门",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult addToDep(
        @Parameter(name = "depId", description = "部门ID", required = true) @PathVariable Long depId,
        @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable Long userId) {
        relationService.addUserToDepartment(userId, depId);
        return AjaxUtil.getInstance().success(null);
    }

    /**
     * 从指定部门移除用户
     * <p>
     * 根据部门ID和用户ID解除用户与部门的关联关系
     * </p>
     *
     * @param depId  部门ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @PostMapping("delEmp/{depId}/{userId}")
    @AjaxResponseType(void.class)
    @Operation(summary = "从部门移除用户", description = "将指定用户从指定部门移除")
    @ApiResponse(responseCode = "200", description = "成功从部门移除用户",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult deleteFromDep(
        @Parameter(name = "depId", description = "部门ID", required = true) @PathVariable Long depId,
        @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable Long userId) {
        relationService.deleteUserFromDepartment(userId, depId);
        return AjaxUtil.getInstance().success(null);
    }

    /**
     * 获取指定部门的员工列表
     * <p>
     * 根据部门ID查询该部门下的所有员工信息
     * </p>
     *
     * @param depId 部门ID
     * @return 部门员工列表
     */
    @GetMapping("employee/{depId}")
    @AjaxResponseType(value = java.util.List.class, genericArgs = {Object.class})
    @Operation(summary = "获取部门员工列表", description = "根据部门ID查询该部门下的所有员工")
    @ApiResponse(responseCode = "200", description = "成功获取部门员工列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult getEmployee(
        @Parameter(name = "depId", description = "部门ID", required = true) @PathVariable Long depId) {
        return AjaxUtil.getInstance().success(relationService.queryDepartmentUsers(depId));
    }

    /**
     * 获取业务名称
     * <p>
     * 实现BaseController接口的方法，返回当前控制器的业务名称
     * </p>
     *
     * @return 业务名称
     */
    @Override
    public String getBusinessName() {
        return "常规部门功能";
    }
}
