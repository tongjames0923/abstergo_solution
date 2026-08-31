package com.tbs.app.controller.privates;

import com.tbs.app.service.role.ISystemRoleService;
import com.tbs.app.web.AbstractCrudController;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.shared.enums.SystemRoleFlagEnum;
import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.criteria.CommonSystemRoleQueryCriteria;
import com.tbs.shared.pojo.entity.SystemRoleEntity;
import com.tbs.common.data.PageVO;
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
 * 权限管理控制器
 * <p>
 * 该控制器用于管理系统权限，提供权限的基本增删改查操作
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@RestController
@RequestMapping("private/admin/role")

@Tag(name = "权限管理接口", description = "权限管理接口")
public class RoleController extends AbstractCrudController<SystemRoleEntity, Long, CommonSystemRoleQueryCriteria> {

	@Resource
	private ISystemRoleService roleService;

	@Override
	public IServiceTemplate<SystemRoleEntity, Long> getService() {
		return roleService;
	}

	/**
	 * 根据ID获取权限信息
	 *
	 * @param id 权限ID
	 * @return 权限信息
	 */
	@Override
	@GetMapping("get/{id}")
	@AjaxResponseType(SystemRoleEntity.class)
	@Operation(summary = "获取权限信息", description = "根据权限ID获取权限详细信息")
	@ApiResponse(responseCode = "200", description = "成功获取权限信息",
		content = @Content(mediaType = "application/json",
			schema = @Schema(implementation = AjaxResult.class)))
	@PreAuthorize("@preAuthUtils.accountActived")
	public AjaxResult get(@Parameter(name = "id", description = "权限ID", required = true) @PathVariable Long id) {
		return super.get(id);
	}

	/**
	 * 保存权限信息
	 *
	 * @param entity 权限实体对象
	 * @return 保存后的权限信息
	 */
	@Override
	@PostMapping("save")
	@AjaxResponseType(SystemRoleEntity.class)
	@Operation(summary = "保存权限信息", description = "创建或更新权限信息")
	@ApiResponse(responseCode = "200", description = "成功保存权限信息",
		content = @Content(mediaType = "application/json",
			schema = @Schema(implementation = AjaxResult.class)))
	@PreAuthorize("@preAuthUtils.accountActived")
	public AjaxResult save(
			@Parameter(name = "entity", description = "权限信息", required = true) @RequestBody SystemRoleEntity entity) {
		return super.save(entity);
	}

	/**
	 * 删除权限
	 *
	 * @param id 权限ID
	 * @return 删除结果
	 */
	@Override
	@PostMapping("delete/{id}")
	@AjaxResponseType(void.class)
	@Operation(summary = "删除权限", description = "根据权限ID删除权限")
	@ApiResponse(responseCode = "200", description = "成功删除权限",
		content = @Content(mediaType = "application/json",
			schema = @Schema(implementation = AjaxResult.class)))
	@PreAuthorize("@preAuthUtils.accountActived")
	public AjaxResult delete(@Parameter(name = "id", description = "权限ID", required = true) @PathVariable Long id) {
		return super.delete(id);
	}

	/**
	 * 查询权限列表
	 *
	 * @param criteria 查询条件
	 * @param pageNum  页码
	 * @param pageSize 每页大小
	 * @return 权限列表分页结果
	 */
	@Override
	@PostMapping("query")
	@AjaxResponseType(PageVO.class)
	@Operation(summary = "查询权限列表", description = "根据条件查询权限列表，支持分页")
	@ApiResponse(responseCode = "200", description = "成功查询权限列表",
		content = @Content(mediaType = "application/json",
			schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult query(
			@Parameter(name = "criteria", description = "查询条件") @RequestBody CommonSystemRoleQueryCriteria criteria,
			@Parameter(name = "pageNum", description = "页码") @RequestParam(required = false) Long pageNum,
			@Parameter(name = "pageSize", description = "每页大小") @RequestParam(required = false) Long pageSize) {
		return super.query(criteria, pageNum, pageSize);
	}

	@GetMapping("/enable/{id}")
	@AjaxResponseType(void.class)
	@Operation(summary = "启用权限", description = "启用权限")
	@ApiResponse(responseCode = "200", description = "成功启用权限",
		content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
	@PreAuthorize("@preAuthUtils.accountActived")
	public AjaxResult enable(@Parameter(name = "id", description = "权限ID", required = true) @PathVariable Long id) {
		SystemRoleEntity entity = roleService.queryById(id);
		if (entity == null) {
			return AjaxUtil.getInstance().error("权限不存在");
		}
		entity.setFlag(SystemRoleFlagEnum.ACTIVE.getCode());
		roleService.save(entity);
		return AjaxUtil.getInstance().success("启用成功", null);
	}

	@GetMapping("/disable/{id}")
	@AjaxResponseType(void.class)
	@Operation(summary = "禁用权限", description = "禁用权限")
	@ApiResponse(responseCode = "200", description = "成功禁用权限",
		content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
	@PreAuthorize("@preAuthUtils.accountActived")
	public AjaxResult disable(@Parameter(name = "id", description = "权限ID", required = true) @PathVariable Long id) {
		SystemRoleEntity entity = roleService.queryById(id);
		if (entity == null) {
			return AjaxUtil.getInstance().error("权限不存在");
		}
		entity.setFlag(SystemRoleFlagEnum.DISABLE.getCode());
		roleService.save(entity);
		return AjaxUtil.getInstance().success("禁用成功", null);
	}


	@Override
	public String getBusinessName() {
		return "权限管理";
	}
}
