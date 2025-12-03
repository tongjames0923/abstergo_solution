package com.tbs.app.controller.privates;

import com.tbs.app.service.role.ISystemRoleService;
import com.tbs.shared.constant.RoleConstants;
import com.tbs.app.web.AbstractCrudController;
import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.criteria.CommonSystemRoleQueryCriteria;
import com.tbs.shared.pojo.entity.SystemRoleEntity;
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
 * 角色管理控制器
 * <p>
 * 该控制器用于管理系统角色，提供角色的基本增删改查操作
 * </p>
 *
 * @author tongj
 */
@RestController
@RequestMapping("private/admin/role")
@PreAuthorize("hasAuthority('" + RoleConstants.SUPER_ADMIN_ROLE_ID + "')")
@Tag(name = "角色管理接口", description = "角色管理接口")
public class RoleController extends AbstractCrudController<SystemRoleEntity, Long, CommonSystemRoleQueryCriteria> {

	@Resource
	private ISystemRoleService roleService;

	@Override
	public IServiceTemplate<SystemRoleEntity, Long> getService() {
		return roleService;
	}

	/**
	 * 根据ID获取角色信息
	 *
	 * @param id 角色ID
	 * @return 角色信息
	 */
	@Override
	@GetMapping("get/{id}")
	@Operation(summary = "获取角色信息", description = "根据角色ID获取角色详细信息")
	@ApiResponse(responseCode = "200", description = "成功获取角色信息",
		content = @Content(mediaType = "application/json",
			schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult get(@Parameter(name = "id", description = "角色ID", required = true) @PathVariable Long id) {
		return super.get(id);
	}

	/**
	 * 保存角色信息
	 *
	 * @param entity 角色实体对象
	 * @return 保存后的角色信息
	 */
	@Override
	@PostMapping("save")
	@Operation(summary = "保存角色信息", description = "创建或更新角色信息")
	@ApiResponse(responseCode = "200", description = "成功保存角色信息",
		content = @Content(mediaType = "application/json",
			schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult save(
			@Parameter(name = "entity", description = "角色信息", required = true) @RequestBody SystemRoleEntity entity) {
		return super.save(entity);
	}

	/**
	 * 删除角色
	 *
	 * @param id 角色ID
	 * @return 删除结果
	 */
	@Override
	@PostMapping("delete/{id}")
	@Operation(summary = "删除角色", description = "根据角色ID删除角色")
	@ApiResponse(responseCode = "200", description = "成功删除角色",
		content = @Content(mediaType = "application/json",
			schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult delete(@Parameter(name = "id", description = "角色ID", required = true) @PathVariable Long id) {
		return super.delete(id);
	}

	/**
	 * 查询角色列表
	 *
	 * @param criteria 查询条件
	 * @param pageNum  页码
	 * @param pageSize 每页大小
	 * @return 角色列表分页结果
	 */
	@Override
	@PostMapping("query")
	@Operation(summary = "查询角色列表", description = "根据条件查询角色列表，支持分页")
	@ApiResponse(responseCode = "200", description = "成功查询角色列表",
		content = @Content(mediaType = "application/json",
			schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult query(
			@Parameter(name = "criteria", description = "查询条件") @RequestBody CommonSystemRoleQueryCriteria criteria,
			@Parameter(name = "pageNum", description = "页码") @RequestParam(required = false) Long pageNum,
			@Parameter(name = "pageSize", description = "每页大小") @RequestParam(required = false) Long pageSize) {
		return super.query(criteria, pageNum, pageSize);
	}

	@Override
	public String getBusinessName() {
		return "角色管理";
	}
}
