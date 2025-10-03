package com.tbs.app.controller.privates;

import com.tbs.app.service.company.ISystemCompanyService;
import com.tbs.shared.constant.RoleConstants;
import com.tbs.shared.interfaces.AbstractCrudController;
import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.criteria.CommonSystemCompanyQueryCriteria;
import com.tbs.shared.pojo.entity.SystemCompanyEntity;
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
@Tag(name = "管理员公司管理接口", description = "管理员公司管理接口")
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
    @Operation(summary = "获取公司信息", description = "根据公司ID获取公司详细信息")
    @ApiResponse(responseCode = "200", description = "成功获取公司信息",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult get(@Parameter(name = "id", description = "公司ID", required = true) @PathVariable Long id) {
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
    @Operation(summary = "保存公司信息", description = "创建或更新公司信息")
    @ApiResponse(responseCode = "200", description = "成功保存公司信息",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult save(@Parameter(name = "entity", description = "公司信息",
        required = true) @RequestBody SystemCompanyEntity entity) {
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
    @Operation(summary = "删除公司信息", description = "根据公司ID删除公司信息")
    @ApiResponse(responseCode = "200", description = "成功删除公司信息",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
    public AjaxResult delete(@Parameter(name = "id", description = "公司ID", required = true) @PathVariable Long id) {
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
    @Operation(summary = "查询公司列表", description = "根据条件查询公司列表，支持分页")
    @ApiResponse(responseCode = "200", description = "成功查询公司列表",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult query(
        @Parameter(name = "criteria", description = "查询条件") @RequestBody CommonSystemCompanyQueryCriteria criteria,
        @Parameter(name = "pageNum", description = "页码") @RequestParam(required = false) Long pageNum,
        @Parameter(name = "pageSize", description = "每页大小") @RequestParam(required = false) Long pageSize) {
		return super.query(criteria, pageNum, pageSize);
	}

	@Override
	public String getBusinessName() {
		return "公司业务";
	}
}
