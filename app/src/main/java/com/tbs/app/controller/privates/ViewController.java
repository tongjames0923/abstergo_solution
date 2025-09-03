package com.tbs.app.controller.privates;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tbs.app.service.business.view.IViewService;
import com.tbs.shared.pojo.model.view.FormConfig;
import com.tbs.web.BaseController;

@RestController
@RequestMapping("/private/view")
public class ViewController extends BaseController {

	@Resource
	private IViewService viewService;

	/**
	 * 根据key获取表单配置
	 *
	 * @param key 配置名称
	 * @return 表单配置
	 */
	@GetMapping("/form")
	public FormConfig getForm(@RequestParam String key) {
		return viewService.getForm(key);
	}

	@Override
	public String getBusinessName() {
		return "视图控制器";
	}
}
