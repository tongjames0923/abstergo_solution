package com.tbs.app.controller.privates;

import javax.annotation.Resource;

import com.tbs.shared.constant.RoleConstants;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tbs.app.service.business.department.IDepartmentService;
import com.tbs.shared.interfaces.AbstractCrudController;
import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.criteria.CommonSystemDepartmentQueryCriteria;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.sql.query.impl.QueryPageModel;
import com.tbs.web.result.AjaxResult;

@RestController
@RequestMapping("private/admin/department")
@PreAuthorize("hasAuthority('" + RoleConstants.SUPER_ADMIN_ROLE_ID + "')")
public class PrivateDepartmentController
		extends AbstractCrudController<SystemDepartmentEntity, Long, CommonSystemDepartmentQueryCriteria> {

	@Resource
	private IDepartmentService departmentService;

	@Override
	public IServiceTemplate<SystemDepartmentEntity, Long> getService() {
		return departmentService;
	}

	@Override
	@GetMapping("get/{id}")
	public AjaxResult get(@PathVariable Long id) {
		return super.get(id);
	}

	@Override
	@PostMapping("save")
	public AjaxResult save(@RequestBody SystemDepartmentEntity entity) {
		return super.save(entity);
	}

	@Override
	@GetMapping("delete/{id}")
	public AjaxResult delete(@PathVariable Long id) {
		return super.delete(id);
	}

	@Override
	@PostMapping("query")
	public AjaxResult query(@RequestBody CommonSystemDepartmentQueryCriteria criteria, Long page, Long perNum) {
		return successResult(departmentService.queryBy(criteria, QueryPageModel.of(page, perNum)));
	}

	@Override
	public String getBusinessName() {
		return "部门业务";
	}
}
