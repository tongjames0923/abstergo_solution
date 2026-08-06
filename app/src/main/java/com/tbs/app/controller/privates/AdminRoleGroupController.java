package com.tbs.app.controller.privates;

import com.tbs.app.service.role.ISystemRoleGroupService;
import com.tbs.app.vo.RemoteUserInfoVO;
import com.tbs.app.vo.SystemRoleGroupVO;
import com.tbs.app.web.AbstractCrudController;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.criteria.CommonSystemRoleGroupCriteria;
import com.tbs.shared.pojo.entity.SystemRoleGroupEntity;
import com.tbs.shared.pojo.model.PageVO;
import com.tbs.web.result.AjaxResult;
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
 * 管理员管理角色组数据控制器
 * <p>
 * 该控制器用于管理员管理角色组数据，提供角色组的基本增删改查操作
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/private/role/group")
@Tag(name = "管理员管理角色组数据接口", description = "管理员管理角色组数据接口")
public class AdminRoleGroupController
    extends AbstractCrudController<SystemRoleGroupEntity, Long, CommonSystemRoleGroupCriteria> {

    @Resource
    private ISystemRoleGroupService groupService;

    /**
     * 获取当前控制器的业务名称
     *
     * @return
     */
    @Override
    public String getBusinessName() {
        return "角色组管理";
    }

    /**
     * 获取服务实例 子类必须实现此方法以提供具体的服务实例
     *
     * @return 服务实例，用于执行具体的业务逻辑
     */
    @Override
    public IServiceTemplate<SystemRoleGroupEntity, Long> getService() {
        return groupService;
    }

    /**
     * 根据ID获取角色组数据信息
     *
     * @param id 角色组ID
     * @return 角色组信息
     */
    @Override
    @GetMapping("get/{id}")
    @AjaxResponseType(SystemRoleGroupEntity.class)
    @Operation(summary = "获取角色组数据", description = "根据角色组ID获取角色组数据详细信息")
    @ApiResponse(responseCode = "200", description = "成功获取角色组数据",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult get(@Parameter(name = "id", description = "角色组ID", required = true) @PathVariable Long id) {
        return super.get(id);
    }

    /**
     * 保存角色组数据信息
     *
     * @param entity 角色组实体对象
     * @return 保存后的角色组信息
     */
    @Override
    @PostMapping("save")
    @AjaxResponseType(SystemRoleGroupEntity.class)
    @Operation(summary = "保存角色组数据", description = "创建或更新角色组数据信息")
    @ApiResponse(responseCode = "200", description = "成功保存角色组数据",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult save(@Parameter(name = "entity", description = "角色组信息",
        required = true) @RequestBody SystemRoleGroupEntity entity) {
        return super.save(entity);
    }

    /**
     * 删除角色组数据
     *
     * @param id 角色组ID
     * @return 删除结果
     */
    @Override
    @PostMapping("delete/{id}")
    @AjaxResponseType(void.class)
    @Operation(summary = "删除角色组数据", description = "根据角色组ID删除角色组数据")
    @ApiResponse(responseCode = "200", description = "成功删除角色组数据",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult delete(@Parameter(name = "id", description = "角色组ID", required = true) @PathVariable Long id) {
        return super.delete(id);
    }

    /**
     * 查询角色组数据列表
     *
     * @param criteria 查询条件
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 角色组列表分页结果
     */
    @Override
    @PostMapping("query")
    @AjaxResponseType(PageVO.class)
    @Operation(summary = "查询角色组数据", description = "根据条件查询角色组数据列表，支持分页")
    @ApiResponse(responseCode = "200", description = "成功查询角色组数据列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult query(
        @Parameter(name = "criteria", description = "查询条件") @RequestBody CommonSystemRoleGroupCriteria criteria,
        @Parameter(name = "pageNum", description = "页码") @RequestParam(required = false) Long pageNum,
        @Parameter(name = "pageSize", description = "每页大小") @RequestParam(required = false) Long pageSize) {
        return super.query(criteria, pageNum, pageSize);
    }

    /**
     * 获取角色组下的权限列表
     *
     * @param groupId 角色组ID
     * @return 权限列表
     */
    @GetMapping("roles/{groupId}")
    @Operation(summary = "获取角色组下的权限列表", description = "根据角色组ID获取该角色组下的所有权限列表")
    @ApiResponse(responseCode = "200", description = "成功获取角色组下的权限列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult getRoleList(
        @Parameter(name = "groupId", description = "角色组ID", required = true) @PathVariable Long groupId) {
        return successResult(groupService.getRoleList(groupId));
    }

    /**
     * 添加角色到角色组
     *
     * @param groupId 角色组ID
     * @param roleId  角色ID
     * @return 操作结果
     */
    @PostMapping("roles/{groupId}/{roleId}")
    @AjaxResponseType(void.class)
    @Operation(summary = "添加角色到角色组", description = "将指定角色添加到指定角色组中")
    @ApiResponse(responseCode = "200", description = "成功添加角色到角色组",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult appendRole(
        @Parameter(name = "groupId", description = "角色组ID", required = true) @PathVariable Long groupId,
        @Parameter(name = "roleId", description = "角色ID", required = true) @PathVariable Long roleId) {
        groupService.appendRole(groupId, roleId);
        return successResult(null);
    }

    /**
     * 移除角色组下的角色
     *
     * @param groupId 角色组ID
     * @param roleId  角色ID
     * @return 操作结果
     */
    @DeleteMapping("roles/{groupId}/{roleId}")
    @AjaxResponseType(void.class)
    @Operation(summary = "移除角色组下的角色", description = "从指定角色组中移除指定角色")
    @ApiResponse(responseCode = "200", description = "成功移除角色组下的角色",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult removeRole(
        @Parameter(name = "groupId", description = "角色组ID", required = true) @PathVariable Long groupId,
        @Parameter(name = "roleId", description = "角色ID", required = true) @PathVariable Long roleId) {
        groupService.removeRole(groupId, roleId);
        return successResult(null);
    }

    /**
     * 分配用户到角色组
     *
     * @param groupId 角色组ID
     * @param userId  用户ID
     * @return 操作结果
     */
    @PostMapping("users/{groupId}/{userId}")
    @AjaxResponseType(void.class)
    @Operation(summary = "分配用户到角色组", description = "将指定用户添加到指定角色组中")
    @ApiResponse(responseCode = "200", description = "成功分配用户到角色组",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult applyGroup(
        @Parameter(name = "groupId", description = "角色组ID", required = true) @PathVariable Long groupId,
        @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable Long userId) {
        groupService.applyGroup(groupId, userId);
        return successResult(null);
    }

    /**
     * 移除用户从角色组
     *
     * @param groupId 角色组ID
     * @param userId  用户ID
     * @return 操作结果
     */
    @DeleteMapping("users/{groupId}/{userId}")
    @AjaxResponseType(void.class)
    @Operation(summary = "移除用户从角色组", description = "从指定角色组中移除指定用户")
    @ApiResponse(responseCode = "200", description = "成功移除用户从角色组",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult removeGroup(
        @Parameter(name = "groupId", description = "角色组ID", required = true) @PathVariable Long groupId,
        @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable Long userId) {
        groupService.removeGroup(groupId, userId);
        return successResult(null);
    }

    /**
     * 获取角色组详情（包含权限列表）
     *
     * @param groupId 角色组ID
     * @return 角色组详情
     */
    @GetMapping("detail/{groupId}")
    @AjaxResponseType(SystemRoleGroupVO.class)
    @Operation(summary = "获取角色组详情", description = "根据角色组ID获取角色组详情，包含权限列表")
    @ApiResponse(responseCode = "200", description = "成功获取角色组详情",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult getRoleGroupVO(
        @Parameter(name = "groupId", description = "角色组ID", required = true) @PathVariable Long groupId) {
        return successResult(groupService.getRoleGroupVO(groupId));
    }

    /**
     * 获取用户的角色组列表
     *
     * @param userId 用户ID
     * @return 用户角色组列表
     */
    @GetMapping("user/{userId}")
    @AjaxResponseType(SystemRoleGroupVO.class)
    @Operation(summary = "获取用户的角色组列表", description = "根据用户ID获取该用户所属的所有角色组列表")
    @ApiResponse(responseCode = "200", description = "成功获取用户角色组列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult getUserRoleGroupVOList(
        @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable Long userId) {
        return successResult(groupService.getUserRoleGroupVOList(userId));
    }

    /**
     * 获取角色组下的用户列表
     *
     * @param groupId 角色组ID
     * @return 用户列表
     */
    @GetMapping("users/{groupId}")
    @AjaxResponseType(RemoteUserInfoVO.class)
    @Operation(summary = "获取角色组下的用户列表", description = "根据角色组ID获取该角色组下的所有用户列表")
    @ApiResponse(responseCode = "200", description = "成功获取角色组下的用户列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult getUserList(
        @Parameter(name = "groupId", description = "角色组ID", required = true) @PathVariable Long groupId) {
        return successResult(groupService.getUserList(groupId));
    }

    /**
     * 检查权限是否存在于任意角色组中
     *
     * @param roleId 权限ID
     * @return 检查结果
     */
    @GetMapping("check/role/{roleId}")
    @AjaxResponseType(Boolean.class)
    @Operation(summary = "检查权限是否存在于任意角色组", description = "检查指定权限是否被分配到任意角色组中")
    @ApiResponse(responseCode = "200", description = "成功获取检查结果",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult isRoleInGroupAnyGroup(
        @Parameter(name = "roleId", description = "权限ID", required = true) @PathVariable Long roleId) {
        return successResult(groupService.isRoleInGroupAnyGroup(roleId));
    }

    /**
     * 检查用户是否在任意角色组下
     *
     * @param userId 用户ID
     * @return 检查结果
     */
    @GetMapping("check/user/{userId}")
    @AjaxResponseType(Boolean.class)
    @Operation(summary = "检查用户是否在任意角色组", description = "检查指定用户是否属于任意角色组")
    @ApiResponse(responseCode = "200", description = "成功获取检查结果",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    @PreAuthorize("@preAuthUtils.accountActived")
    public AjaxResult isUserInGroupAnyGroup(
        @Parameter(name = "userId", description = "用户ID", required = true) @PathVariable Long userId) {
        return successResult(groupService.isUserInGroupAnyGroup(userId));
    }
}
