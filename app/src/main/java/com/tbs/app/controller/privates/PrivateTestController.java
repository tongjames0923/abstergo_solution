package com.tbs.app.controller.privates;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;

@RestController
@RequestMapping("/private")
public class PrivateTestController extends BaseController {
	@GetMapping("/test")
	public AjaxResult test(int value) {
		return AjaxUtil.getInstance().custom(468, "测试权限", value);
	}

	@GetMapping("/testAuth")
	public AjaxResult testAuth() {
		return AjaxUtil.getInstance().success(null);
	}

	@Override
	public String getBusinessName() {
		return "测试登录后功能控制器";
	}
}
