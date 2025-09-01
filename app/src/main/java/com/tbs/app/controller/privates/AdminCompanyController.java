package com.tbs.app.controller.privates;

import javax.annotation.Resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tbs.app.service.business.company.ISystemCompanyService;
import com.tbs.shared.constant.RoleConstants;
import com.tbs.shared.interfaces.AbstractCrudController;
import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.criteria.CommonSystemCompanyQueryCriteria;
import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import com.tbs.web.result.AjaxResult;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 管理员公司管理控制器
 * <p>
 * 该控制器用于管理系统中的公司信息，提供公司的基本增删改查操作
 * </p>
 *
 * @author abstergo
 */
@RestController
@RequestMapping("/private/admin/company")
@PreAuthorize("hasAuthority('" + RoleConstants.SUPER_ADMIN_ROLE_ID + "')")
@Api(value = "管理员公司管理", tags = "管理员公司管理接口")
public class AdminCompanyController
		extends AbstractCrudController<SystemCompanyEntity, Long, CommonSystemCompanyQueryCriteria> {

	@Resource
	private ISystemCompanyService companyService;

	@Override
	public IServiceTemplate<SystemCompanyEntity, Long> getService() {
		return companyService;
	}

	/**
	 * 根据ID获取公司信息
	 *
	 * @param id 公司ID
	 * @return 公司信息
	 */
	@Override
	@GetMapping("get/{id}")
	@ApiOperation(value = "获取公司信息", notes = "根据公司ID获取公司详细信息")
	public AjaxResult get(@ApiParam(name = "id", value = "公司ID", required = true) @PathVariable Long id) {
		return super.get(id);
	}

	/**
	 * 保存公司信息
	 *
	 * @param entity 公司实体对象
	 * @return 保存后的公司信息
	 */
	@Override
	@PostMapping("save")
	@ApiOperation(value = "保存公司信息", notes = "创建或更新公司信息")
	public AjaxResult save(
			@ApiParam(name = "entity", value = "公司信息", required = true) @RequestBody SystemCompanyEntity entity) {
		return super.save(entity);
	}

	/**
	 * 删除公司信息
	 *
	 * @param id 公司ID
	 * @return 删除结果
	 */
	@Override
	@PostMapping("delete/{id}")
	@ApiOperation(value = "删除公司信息", notes = "根据公司ID删除公司信息")
	public AjaxResult delete(@ApiParam(name = "id", value = "公司ID", required = true) @PathVariable Long id) {
		return super.delete(id);
	}

	/**
	 * 查询公司列表
	 *
	 * @param criteria 查询条件
	 * @param pageNum  页码
	 * @param pageSize 每页大小
	 * @return 公司列表分页结果
	 */
	@Override
	@PostMapping("query")
	@ApiOperation(value = "查询公司列表", notes = "根据条件查询公司列表，支持分页")
	public AjaxResult query(
			@ApiParam(name = "criteria", value = "查询条件") @RequestBody CommonSystemCompanyQueryCriteria criteria,
			@ApiParam(name = "pageNum", value = "页码") @RequestParam(required = false) Long pageNum,
			@ApiParam(name = "pageSize", value = "每页大小") @RequestParam(required = false) Long pageSize) {
		return super.query(criteria, pageNum, pageSize);
	}

	@Override
	public String getBusinessName() {
		return "公司业务";
	}
}
