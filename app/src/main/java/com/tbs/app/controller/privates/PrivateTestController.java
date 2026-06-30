package com.tbs.app.controller.privates;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import com.tbs.knife4j.annotation.AjaxResponseType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 私有测试控制器
 * <p>
 * 提供需要登录才能访问的测试接口，用于测试权限和认证功能
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/private")
@Tag(name = "测试登录后功能控制器", description = "测试登录后功能控制器")
public class PrivateTestController extends BaseController {
	/**
	 * 测试权限接口
	 * <p>
	 * 用于测试用户是否有权限访问受保护的接口
	 * </p>
	 *
	 * @param value 测试值参数
	 * @return 测试结果
	 */
	@GetMapping("/test")
	@AjaxResponseType(Object.class)
	@Operation(summary = "测试权限", description = "测试权限")
	@ApiResponse(responseCode = "200", description = "测试权限",
		content = @Content(mediaType = "application/json",
			schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult test(@Parameter(name = "value", description = "测试值") int value) {
		return AjaxUtil.getInstance().custom(468, "测试权限", value);
	}

	/**
	 * 测试认证接口
	 * <p>
	 * 用于测试用户是否已成功认证
	 * </p>
	 *
	 * @return 测试认证结果
	 */
	@GetMapping("/testAuth")
	@AjaxResponseType(void.class)
	@Operation(summary = "测试认证", description = "测试认证")
	@ApiResponse(responseCode = "200", description = "测试认证成功",
		content = @Content(mediaType = "application/json",
			schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult testAuth() {
		return AjaxUtil.getInstance().success(null);
	}

	/**
	 * 获取业务名称
	 * <p>
	 * 实现BaseController接口的方法，返回当前控制器的业务名称
	 * </p>
	 *
	 * @return 业务名称
	 */
	@Override
	public String getBusinessName() {
		return "测试登录后功能控制器";
	}
}
