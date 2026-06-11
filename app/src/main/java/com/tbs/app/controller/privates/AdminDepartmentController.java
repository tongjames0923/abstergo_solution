package com.tbs.app.controller.privates;

import com.tbs.app.service.department.ISystemDepartmentService;
import com.tbs.app.service.impl.department.DepartmentTreeStructService;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.common.delegate.IDelegateFlag;
import com.tbs.common.util.framework.DelegateUtil;
import com.tbs.shared.constant.RoleConstants;
import com.tbs.app.web.AbstractCrudController;
import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.criteria.CommonSystemDepartmentQueryCriteria;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.sql.query.impl.QueryPageModel;
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
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * 管理员部门管理控制器
 * <p>
 * 该控制器用于管理系统中的部门信息，提供部门的基本增删改查操作
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/private/admin/department")
@PreAuthorize("hasAuthority('" + RoleConstants.SUPER_ADMIN_ROLE_NAME + "')")
@Tag(name = "管理员部门管理接口", description = "管理员部门管理接口")
public class AdminDepartmentController
		extends AbstractCrudController<SystemDepartmentEntity, Long, CommonSystemDepartmentQueryCriteria> {

	@Resource
	private ISystemDepartmentService departmentService;

	@Resource
	private DepartmentTreeStructService departmentTreeStructService;

	@Override
	public IServiceTemplate<SystemDepartmentEntity, Long> getService() {
		return departmentService;
	}

	/**
	 * 根据ID获取部门信息
	 *
	 * @param id 部门ID
	 * @return 部门信息
	 */
	@Override
	@GetMapping("get/{id}")
    @Operation(summary = "获取部门信息", description = "根据部门ID获取部门详细信息")
    @ApiResponse(responseCode = "200", description = "成功获取部门信息",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult get(@Parameter(name = "id", description = "部门ID", required = true) @PathVariable Long id) {
        SystemDepartmentEntity entity = getService().queryById(id);
		entity.setChildrenCount((long)departmentTreeStructService.getChildren(entity.getId()).size());
        return successResult(entity);
	}

	/**
	 * 保存部门信息
	 *
	 * @param entity 部门实体对象
	 * @return 保存后的部门信息
	 */
	@Override
	@PostMapping("save")
    @Operation(summary = "保存部门信息", description = "创建或更新部门信息")
    @ApiResponse(responseCode = "200", description = "成功保存部门信息",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult save(@Parameter(name = "entity", description = "部门信息",
        required = true) @RequestBody SystemDepartmentEntity entity) {
		return super.save(entity);
	}

	/**
	 * 删除部门信息
	 *
	 * @param id 部门ID
	 * @return 删除结果
	 */
	@Override
	@PostMapping("delete/{id}")
    @Operation(summary = "删除部门信息", description = "根据部门ID删除部门信息")
    @ApiResponse(responseCode = "200", description = "成功删除部门信息",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult delete(@Parameter(name = "id", description = "部门ID", required = true) @PathVariable Long id) {
		return super.delete(id);
	}

	/**
	 * 查询部门列表
	 *
	 * @param criteria 查询条件
	 * @param pageNum  页码
	 * @param pageSize 每页大小
	 * @return 部门列表分页结果
	 */
	@Override
	@PostMapping("query")
    @Operation(summary = "查询部门列表", description = "根据条件查询部门列表，支持分页")
    @ApiResponse(responseCode = "200", description = "成功查询部门列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult query(
        @Parameter(name = "criteria", description = "查询条件") @RequestBody CommonSystemDepartmentQueryCriteria criteria,
        @Parameter(name = "pageNum", description = "页码") @RequestParam(required = false) Long pageNum,
        @Parameter(name = "pageSize", description = "每页大小") @RequestParam(required = false) Long pageSize) {
        var data = getService().queryAndPage(criteria, QueryPageModel.of(pageNum, pageSize));
        var ls = new LinkedList<IDelegateFlag>();
        for (var entity : data.getData()) {
            ls.add(DelegateUtil.getInstance().delegateWithTarget(entity).build(AsyncDelegateInvoker.class)
                .takeIn(SystemDepartmentEntity::setChildrenCount, () -> {
                    return (long)departmentTreeStructService.getChildren(entity.getId()).size();
                }).executeOnce());
        }
        for (var i : ls) {
            i.waitUntilFinished();
        }
        return successResult(data);
	}

    /**
     * 移动部门到指定父级部门下
     *
     * @param parent 父级部门ID
     * @param child  子级部门ID
     * @return 操作结果
     */
    @PostMapping("moveDepartmentTo")
    @Operation(summary = "移动部门到指定父级", description = "将一个部门移动到另一个部门下作为其子级部门")
    @ApiResponse(responseCode = "200", description = "成功移动部门",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult moveDepartmentTo(
        @Parameter(name = "parent", description = "父级部门ID", required = true) @RequestParam Long parent,
        @Parameter(name = "child", description = "子级部门ID", required = true) @RequestParam Long child) {
		departmentTreeStructService.setToTree(child, parent);
        return successResult(null);
    }

    /**
     * 获取指定部门的所有子级部门列表
     *
     * @param parent 父级部门ID
     * @return 子级部门列表
     */
    @GetMapping("listChildren/{parent}")
    @Operation(summary = "获取子级部门列表", description = "获取指定部门的所有直接子级部门列表")
    @ApiResponse(responseCode = "200", description = "成功获取子级部门列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult listChildren(
        @Parameter(name = "parent", description = "父级部门ID", required = true) @PathVariable Long parent) {
        var data = departmentTreeStructService.getChildren(parent).stream().map((p) -> {
			return departmentService.queryById(p.getEntityId());
		}).collect(Collectors.toList());
        var ls = new LinkedList<IDelegateFlag>();
        for (var entity : data) {
            ls.add(DelegateUtil.getInstance().delegateWithTarget(entity).build(AsyncDelegateInvoker.class)
				.takeIn(SystemDepartmentEntity::setChildrenCount, () -> {
					return (long)(departmentTreeStructService.getChildren(entity.getId()).size());
                }).executeOnce());
        }
        for (var i : ls) {
            i.waitUntilFinished();
        }
        return successResult(data);
    }

	@Override
	public String getBusinessName() {
		return "部门业务";
	}
}
