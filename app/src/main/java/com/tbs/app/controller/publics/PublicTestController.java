package com.tbs.app.controller.publics;

import com.tbs.app.service.TestService;
import com.tbs.app.service.role.ISystemRoleService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.cache.annotation.ExpireOn;
import com.tbs.shared.pojo.criteria.CommonSystemUserQueryCriteria;
import com.tbs.shared.pojo.entity.SystemRoleEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.sql.query.impl.QueryPageModel;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author abstergo
 */
@RestController
@RequestMapping("/public")
@Tag(name = "公共测试控制器", description = "公共测试控制器")
public class PublicTestController extends BaseController {

	@Resource
	TestService service;

	@Cacheable(key = "#value", value = "test")
	@ExpireOn(value = 5)
	@GetMapping("/test")
	@Operation(summary = "测试缓存", description = "测试缓存功能")
	@ApiResponse(responseCode = "200", description = "测试成功", 
		content = @Content(mediaType = "application/json", 
			schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult test(@Parameter(name = "value", description = "测试值") String value) throws InterruptedException {
		return AjaxUtil.getInstance().success(service.test());
	}

	@GetMapping("/test1")
	@Operation(summary = "简单测试", description = "简单测试功能")
	@ApiResponse(responseCode = "200", description = "测试成功", 
		content = @Content(mediaType = "application/json", 
			schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult test1() throws InterruptedException {
		return AjaxUtil.getInstance().success(1);
	}

	@Resource
	private ISystemUserService userService;

	@PostMapping("saveUser")
	@Operation(summary = "保存用户", description = "保存用户信息")
	@ApiResponse(responseCode = "200", description = "保存成功", 
		content = @Content(mediaType = "application/json", 
			schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult createUser(@Parameter(description = "系统用户实体") @RequestBody SystemUserEntity systemUserEntity) {
		return AjaxUtil.getInstance().success("保存成功", userService.save(systemUserEntity));
	}

	@PostMapping("queryUser")
	@Operation(summary = "查询用户", description = "根据条件查询用户列表")
	@ApiResponse(responseCode = "200", description = "查询成功", 
		content = @Content(mediaType = "application/json", 
			schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult queryUser(@Parameter(description = "查询条件") @RequestBody CommonSystemUserQueryCriteria criteria, 
			@Parameter(name = "pageNum", description = "页码") long pageNum, 
			@Parameter(name = "pageSize", description = "每页大小") long pageSize) {
		return AjaxUtil.getInstance().success("查询成功",
				userService.queryAndPage(criteria, QueryPageModel.of(pageNum, pageSize)));
	}

	@Resource
	private ISystemRoleService roleService;

	@PostMapping("saveRole")
	@Operation(summary = "保存角色", description = "保存角色信息")
	@ApiResponse(responseCode = "200", description = "保存成功", 
		content = @Content(mediaType = "application/json", 
			schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult createRole(@Parameter(description = "角色实体") @RequestBody SystemRoleEntity role) {
		return AjaxUtil.getInstance().success("保存成功", roleService.save(role));
	}

	@Override
	public String getBusinessName() {
		return "测试";
	}

	// @Resource
	// private IUserAuthService userAuthService;

	// @GetMapping("/login")
	// public AjaxResult login(Long userId, String password) {
	// return
	// AjaxUtil.getInstance().success(userAuthService.login(String.valueOf(userId),
	// password));
	// }
}