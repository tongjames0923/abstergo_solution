package com.tbs.app.controller.privates;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tbs.shared.pojo.model.view.DateRangeFormField;
import com.tbs.shared.pojo.model.view.DynamicOptionsFormField;
import com.tbs.shared.pojo.model.view.FormConfig;
import com.tbs.shared.pojo.model.view.FormField;
import com.tbs.web.BaseController;

@RestController
@RequestMapping("/private/view")
public class ViewController extends BaseController {

	/**
	 * 根据key获取表单配置
	 *
	 * @param key 配置名称
	 * @return 表单配置
	 */
	@GetMapping("/getSearchForm")
	public FormConfig getSearchForm(@RequestParam String key) {
		if ("userSearchForm".equals(key)) {
			return buildUserSearchFormConfig();
		}
		// 可以根据需要添加其他表单配置
		return null;
	}

	/**
	 * 构建用户搜索表单配置
	 *
	 * @return 用户搜索表单配置
	 */
	private FormConfig buildUserSearchFormConfig() {
		FormConfig formConfig = new FormConfig();
		formConfig.setExpandable(true);
		formConfig.setExpanded(true);
		formConfig.setGutter(20);
		formConfig.setLabelWidth("100px");

		List<FormField> fields = new ArrayList<>();

		// 用户ID字段
		FormField idField = new FormField();
		idField.setProp("idEquals");
		idField.setLabel("用户ID");
		idField.setType("input");
		idField.setPlaceholder("请输入用户ID");
		idField.setPrefixIcon("el-icon-search");
		fields.add(idField);

		// 用户名字段
		FormField userNameField = new FormField();
		userNameField.setProp("userNameEquals");
		userNameField.setLabel("用户名");
		userNameField.setType("input");
		userNameField.setPlaceholder("请输入用户名");
		userNameField.setPrefixIcon("el-icon-user");
		fields.add(userNameField);

		// 昵称字段
		FormField nickNameField = new FormField();
		nickNameField.setProp("nickNameLike");
		nickNameField.setLabel("昵称");
		nickNameField.setType("input");
		nickNameField.setPlaceholder("请输入昵称");
		nickNameField.setPrefixIcon("el-icon-star-off");
		fields.add(nickNameField);

		// 用户状态字段
		DynamicOptionsFormField flagField = new DynamicOptionsFormField();
		flagField.setProp("flagEquals");
		flagField.setLabel("用户状态");
		flagField.setApiUrl("/private/user/statusOptions");
		flagField.setPlaceholder("请选择用户状态");
		fields.add(flagField);

		// 创建时间范围字段
		DateRangeFormField createTimeField = new DateRangeFormField();
		createTimeField.setProp("createTimeRange");
		createTimeField.setLabel("创建时间");
		createTimeField.setValueFormat("yyyy-MM-dd HH:mm:ss");
		createTimeField.setFormat("yyyy-MM-dd");
		createTimeField.setStartPlaceholder("开始日期");
		createTimeField.setEndPlaceholder("结束日期");
		fields.add(createTimeField);

		// 更新时间范围字段
		DateRangeFormField updateTimeField = new DateRangeFormField();
		updateTimeField.setProp("updateTimeRange");
		updateTimeField.setLabel("更新时间");
		updateTimeField.setValueFormat("yyyy-MM-dd HH:mm:ss");
		updateTimeField.setFormat("yyyy-MM-dd");
		updateTimeField.setStartPlaceholder("开始日期");
		updateTimeField.setEndPlaceholder("结束日期");
		fields.add(updateTimeField);

		formConfig.setFields(fields);
		return formConfig;
	}

	@Override
	public String getBusinessName() {
		return "视图控制器";
	}
}
