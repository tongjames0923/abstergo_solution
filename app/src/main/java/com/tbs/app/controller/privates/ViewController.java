package com.tbs.app.controller.privates;

import com.tbs.app.service.business.view.IViewService;
import com.tbs.shared.pojo.model.view.FormConfig;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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

    /**
     * 保存表单配置
     *
     * @param formConfig 表单配置
     * @return 保存结果
     */
    @PostMapping("saveForm")
    public AjaxResult saveForm(@RequestBody FormConfig formConfig) {
        return successResult(1);
    }

	@Override
	public String getBusinessName() {
		return "视图控制器";
	}
}
