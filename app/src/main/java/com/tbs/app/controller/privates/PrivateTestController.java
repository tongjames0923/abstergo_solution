package com.tbs.app.controller.privates;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/private")
@Tag(name = "测试登录后功能控制器", description = "测试登录后功能控制器")
public class PrivateTestController extends BaseController {
	@GetMapping("/test")
	@Operation(summary = "测试权限", description = "测试权限")
	@ApiResponse(responseCode = "200", description = "测试权限",
		content = @Content(mediaType = "application/json",
			schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult test(@Parameter(name = "value", description = "测试值") int value) {
		return AjaxUtil.getInstance().custom(468, "测试权限", value);
	}

	@GetMapping("/testAuth")
	@Operation(summary = "测试认证", description = "测试认证")
	@ApiResponse(responseCode = "200", description = "测试认证成功",
		content = @Content(mediaType = "application/json",
			schema = @Schema(implementation = AjaxResult.class)))
	public AjaxResult testAuth() {
		return AjaxUtil.getInstance().success(null);
	}

	@Override
	public String getBusinessName() {
		return "测试登录后功能控制器";
	}
}
