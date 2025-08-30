package com.tbs.app.controller.privates;

import javax.annotation.Resource;

import com.tbs.shared.constant.RoleConstants;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tbs.app.service.business.role.ISystemRoleService;
import com.tbs.shared.interfaces.AbstractCrudController;
import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.criteria.CommonSystemRoleQueryCriteria;
import com.tbs.shared.pojo.entity.SystemRoleEntity;
import com.tbs.web.result.AjaxResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

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
@Api(value = "角色管理", tags = "角色管理接口")
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
	@ApiOperation(value = "获取角色信息", notes = "根据角色ID获取角色详细信息")
	public AjaxResult get(@ApiParam(name = "id", value = "角色ID", required = true) @PathVariable Long id) {
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
	@ApiOperation(value = "保存角色信息", notes = "创建或更新角色信息")
	public AjaxResult save(
			@ApiParam(name = "entity", value = "角色信息", required = true) @RequestBody SystemRoleEntity entity) {
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
	@ApiOperation(value = "删除角色", notes = "根据角色ID删除角色")
	public AjaxResult delete(@ApiParam(name = "id", value = "角色ID", required = true) @PathVariable Long id) {
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
	@ApiOperation(value = "查询角色列表", notes = "根据条件查询角色列表，支持分页")
	public AjaxResult query(
			@ApiParam(name = "criteria", value = "查询条件") @RequestBody CommonSystemRoleQueryCriteria criteria,
			@ApiParam(name = "pageNum", value = "页码") @RequestParam(required = false) Long pageNum,
			@ApiParam(name = "pageSize", value = "每页大小") @RequestParam(required = false) Long pageSize) {
		return super.query(criteria, pageNum, pageSize);
	}

	@Override
	public String getBusinessName() {
		return "角色管理";
	}
}
