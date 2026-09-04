package com.tbs.app.controller.privates;

import com.tbs.app.service.role.ISystemRoleService;
import com.tbs.app.web.AbstractCrudController;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.shared.enums.SystemRoleFlagEnum;
import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.criteria.CommonSystemRoleQueryCriteria;
import com.tbs.shared.pojo.entity.SystemRoleEntity;
import com.tbs.common.data.PageVO;
import com.tbs.common.i18n.I18nUtil;
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
 * 该控制器用于管理系统权限，提供权限的基本增删改查操作。
 * 增删改查主体能力继承自{@link AbstractCrudController}，本类仅负责接口暴露、参数声明与权限启用/禁用两个状态切换接口。
 * 接口位于私有分区（路径前缀private），除分页查询外的方法均要求账号处于已激活状态
 * （{@code @PreAuthorize("@preAuthUtils.accountActived")}）。
 * 业务校验（类型、编码唯一性、状态合法性、系统内置权限保护等）全部下沉至
 * {@link ISystemRoleService}的实现类中，控制器不做规则判断
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@RestController
@RequestMapping("private/admin/role")

@Tag(name = "权限管理接口", description = "权限管理接口")
public class RoleController extends AbstractCrudController<SystemRoleEntity, Long, CommonSystemRoleQueryCriteria> {

	/**
	 * 国际化消息键：权限不存在
	 * <p>
	 * 复用系统模块已有键，与权限组服务中的同名校验保持文案一致
	 * </p>
	 */
	private static final String I18N_PERMISSION_NOT_EXISTS = "permission.not.exists";

	/**
	 * 国际化消息键：权限启用成功
	 */
	private static final String I18N_PERMISSION_ENABLE_SUCCESS = "permission.enable.success";

	/**
	 * 国际化消息键：权限禁用成功
	 */
	private static final String I18N_PERMISSION_DISABLE_SUCCESS = "permission.disable.success";

	/**
	 * 国际化消息键：权限管理业务名称
	 */
	private static final String I18N_PERMISSION_BUSINESS_NAME = "permission.business.name";

	/**
	 * 权限服务
	 * <p>
	 * 承载权限实体的增删改查与按编码查询能力，由服务模板统一提供逻辑删除与审计支持
	 * </p>
	 */
	@Resource
	private ISystemRoleService roleService;

	/**
	 * 获取权限服务实例
	 * <p>
	 * 基类{@link AbstractCrudController}的CRUD接口通过该实例执行具体的业务操作
	 * </p>
	 *
	 * @return 权限服务实例
	 */
	@Override
	public IServiceTemplate<SystemRoleEntity, Long> getService() {
		return roleService;
	}

	/**
	 * 根据ID获取权限信息
	 * <p>
	 * 查询结果由服务层按权限ID缓存，权限不存在时data返回null
	 * </p>
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
	 * <p>
	 * 实体携带ID时按ID更新，未携带ID时按新增处理。新增由服务层强制置为激活状态，
	 * 并校验权限编码唯一性；更新仅覆盖本次传入的非空字段，系统内置权限（flag=2）拒绝修改。
	 * 响应data为保存后的权限实体列表
	 * </p>
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
	 * <p>
	 * 采用逻辑删除（状态置为已删除），物理记录不移除。系统内置权限与被权限组引用的权限由服务层拒绝删除
	 * </p>
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
	 * <p>
	 * 分页参数走query，查询条件走请求体。查询条件类标注了逻辑删除过滤，
	 * 结果中不包含已删除的权限记录
	 * </p>
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

	/**
	 * 启用权限
	 * <p>
	 * 将指定权限的状态标识置为激活，使其可被分配给用户或权限组。
	 * 权限不存在时返回错误响应；系统内置权限在保存阶段由服务层拒绝修改，抛出业务异常
	 * </p>
	 *
	 * @param id 权限ID
	 * @return 操作结果
	 */
	@GetMapping("/enable/{id}")
	@AjaxResponseType(void.class)
	@Operation(summary = "启用权限", description = "启用权限")
	@ApiResponse(responseCode = "200", description = "成功启用权限",
		content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
	@PreAuthorize("@preAuthUtils.accountActived")
	public AjaxResult enable(@Parameter(name = "id", description = "权限ID", required = true) @PathVariable Long id) {
		SystemRoleEntity entity = roleService.queryById(id);
		if (entity == null) {
			return AjaxUtil.getInstance().error(I18nUtil.get(I18N_PERMISSION_NOT_EXISTS), null);
		}
		// 仅改状态标识后整存，其余字段沿用库中原值，由服务层的差异更新逻辑决定实际落库字段
		entity.setFlag(SystemRoleFlagEnum.ACTIVE.getCode());
		roleService.save(entity);
		return AjaxUtil.getInstance().success(I18nUtil.get(I18N_PERMISSION_ENABLE_SUCCESS), null);
	}

	/**
	 * 禁用权限
	 * <p>
	 * 将指定权限的状态标识置为失效，已分配该权限的主体在鉴权时不再命中，但历史分配关系保留。
	 * 权限不存在时返回错误响应；系统内置权限在保存阶段由服务层拒绝修改，抛出业务异常
	 * </p>
	 *
	 * @param id 权限ID
	 * @return 操作结果
	 */
	@GetMapping("/disable/{id}")
	@AjaxResponseType(void.class)
	@Operation(summary = "禁用权限", description = "禁用权限")
	@ApiResponse(responseCode = "200", description = "成功禁用权限",
		content = @Content(mediaType = "application/json", schema = @Schema(implementation = AjaxResult.class)))
	@PreAuthorize("@preAuthUtils.accountActived")
	public AjaxResult disable(@Parameter(name = "id", description = "权限ID", required = true) @PathVariable Long id) {
		SystemRoleEntity entity = roleService.queryById(id);
		if (entity == null) {
			return AjaxUtil.getInstance().error(I18nUtil.get(I18N_PERMISSION_NOT_EXISTS), null);
		}
		// 仅改状态标识后整存，其余字段沿用库中原值，由服务层的差异更新逻辑决定实际落库字段
		entity.setFlag(SystemRoleFlagEnum.DISABLE.getCode());
		roleService.save(entity);
		return AjaxUtil.getInstance().success(I18nUtil.get(I18N_PERMISSION_DISABLE_SUCCESS), null);
	}

	/**
	 * 获取业务名称
	 * <p>
	 * 供操作日志与审计场景标识当前控制器归属的业务域，按当前请求语言环境解析
	 * </p>
	 *
	 * @return 业务名称
	 */
	@Override
	public String getBusinessName() {
		return I18nUtil.get(I18N_PERMISSION_BUSINESS_NAME);
	}
}
