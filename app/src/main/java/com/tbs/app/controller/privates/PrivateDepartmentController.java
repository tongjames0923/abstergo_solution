package com.tbs.app.controller.privates;

import javax.annotation.Resource;

import com.tbs.sql.model.QueryPageModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tbs.app.service.business.department.IDepartmentService;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.pojo.criteria.CommonSystemDepartmentQueryCriteria;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;

@RestController
@RequestMapping("private/admin/department")
@PreAuthorize("hasAuthority('" + CommonConstant.SUPER_ADMIN_ROLE_NAME + "')")
public class PrivateDepartmentController extends BaseController {

	@Resource
	private IDepartmentService departmentService;

	@PostMapping("query")
	public AjaxResult query(@RequestBody CommonSystemDepartmentQueryCriteria criteria, Long page, Long perNum) {
		return successResult(departmentService.queryBy(criteria, QueryPageModel.of(page, perNum)));
	}

	@Override
	public String getBusinessName() {
		return "部门业务";
	}
}
