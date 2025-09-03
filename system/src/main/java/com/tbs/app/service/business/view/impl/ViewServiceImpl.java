package com.tbs.app.service.business.view.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tbs.app.service.business.view.IViewService;
import com.tbs.shared.enums.SystemCompanyFlagEnum;
import com.tbs.shared.pojo.model.view.*;

public class ViewServiceImpl implements IViewService {
	@Override
	public FormConfig getForm(String key) {
		if ("userSearchForm".equals(key)) {
			return buildUserSearchFormConfig();
		} else if ("userEditForm".equals(key)) {
			return buildUserEditFormConfig();
		} else if ("userAddForm".equals(key)) {
			return buildUserAddFormConfig();
		} else if ("companySearchForm".equals(key)) {
			return buildCompanySearchFormConfig();
		}
        else if("companyAddForm".equals(key))
        {
            return buildCompanyAddFormConfig();
        }
		// 可以根据需要添加其他表单配置
		return null;
	}

    private FormConfig buildCompanyAddFormConfig() {
        FormConfig formConfig = new FormConfig();
        formConfig.setExpandable(false);
        formConfig.setGutter(20);
        formConfig.setLabelWidth("80px");
        formConfig.setSpan(24);

        List<FormField> fields = new ArrayList<>();

        // 公司名称字段
        FormField companyNameField = new FormField();
        companyNameField.setProp("companyName");
        companyNameField.setLabel("公司名称");
        companyNameField.setType("input");
        companyNameField.setPlaceholder("请输入公司名称");
        companyNameField.setPrefixIcon("el-icon-office-building");
        companyNameField.setProps(Map.of("size", "medium"));
        fields.add(companyNameField);

        // 税号字段
        FormField taxNumberField = new FormField();
        taxNumberField.setProp("taxNumber");
        taxNumberField.setLabel("税号");
        taxNumberField.setType("input");
        taxNumberField.setPlaceholder("请输入税号");
        taxNumberField.setPrefixIcon("el-icon-document");
        taxNumberField.setProps(Map.of("size", "medium"));
        fields.add(taxNumberField);

        // 公司logo字段
        FormField logoImageField = new FormField();
        logoImageField.setProp("logoImage");
        logoImageField.setLabel("公司Logo");
        logoImageField.setType("input");
        logoImageField.setPlaceholder("请输入Logo图片URL");
        logoImageField.setPrefixIcon("el-icon-picture-outline");
        logoImageField.setProps(Map.of("size", "medium"));
        fields.add(logoImageField);

        // 描述字段
        FormField descriptionField = new FormField();
        descriptionField.setProp("description");
        descriptionField.setLabel("描述");
        descriptionField.setType("input");
        descriptionField.setPlaceholder("请输入公司描述");
        descriptionField.setProps(Map.of("type", "textarea", "rows", 3, "size", "medium"));
        fields.add(descriptionField);

        formConfig.setFields(fields);
        return formConfig;
    }

	private FormConfig buildUserAddFormConfig() {
		FormConfig formConfig = new FormConfig();
		formConfig.setExpandable(false);
		formConfig.setGutter(20);
		formConfig.setLabelWidth("80px");
		formConfig.setSpan(24);

		List<FormField> fields = new ArrayList<>();

		// 用户名字段
		FormField userNameField = new FormField();
		userNameField.setProp("userName");
		userNameField.setLabel("用户名");
		userNameField.setType("input");
		userNameField.setPlaceholder("请输入用户名");
		userNameField.setPrefixIcon("el-icon-user");
		userNameField.setProps(Map.of("size", "medium"));
		fields.add(userNameField);

		// 昵称字段
		FormField nickNameField = new FormField();
		nickNameField.setProp("nickName");
		nickNameField.setLabel("昵称");
		nickNameField.setType("input");
		nickNameField.setPlaceholder("请输入昵称");
		nickNameField.setPrefixIcon("el-icon-star-off");
		nickNameField.setProps(Map.of("size", "medium"));
		fields.add(nickNameField);

		// 密码字段
		FormField passwordField = new FormField();
		passwordField.setProp("password");
		passwordField.setLabel("密码");
		passwordField.setType("input");
		passwordField.setPlaceholder("请输入密码");
		passwordField.setPrefixIcon("el-icon-lock");
		passwordField.setProps(Map.of("type", "password", "showPassword", true, "size", "medium"));
		fields.add(passwordField);

		// 备注字段
		FormField remarkField = new FormField();
		remarkField.setProp("remark");
		remarkField.setLabel("备注");
		remarkField.setType("input");
		remarkField.setPlaceholder("请输入备注信息");
		remarkField.setProps(Map.of("type", "textarea", "rows", 2, "size", "medium"));
		fields.add(remarkField);

		// 头像字段 (自定义类型)
		FormField avatarField = new FormField();
		avatarField.setProp("avatar");
		avatarField.setLabel("头像");
		avatarField.setType("urlImage");
		fields.add(avatarField);

		formConfig.setFields(fields);
		return formConfig;
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

	/**
	 * 构建用户编辑表单配置
	 *
	 * @return 用户编辑表单配置
	 */
	private FormConfig buildUserEditFormConfig() {
		FormConfig formConfig = new FormConfig();
		formConfig.setExpandable(false);
		formConfig.setGutter(20);
		formConfig.setLabelWidth("80px");
		formConfig.setSpan(24);

		List<FormField> fields = new ArrayList<>();

		// 用户名字段
		FormField userNameField = new FormField();
		userNameField.setProp("userName");
		userNameField.setLabel("用户名");
		userNameField.setType("input");
		userNameField.setPlaceholder("请输入用户名");
		userNameField.setPrefixIcon("el-icon-user");
		userNameField.setProps(Map.of("size", "medium"));
		fields.add(userNameField);

		// 昵称字段
		FormField nickNameField = new FormField();
		nickNameField.setProp("nickName");
		nickNameField.setLabel("昵称");
		nickNameField.setType("input");
		nickNameField.setPlaceholder("请输入昵称");
		nickNameField.setPrefixIcon("el-icon-star-off");
		nickNameField.setProps(Map.of("size", "medium"));
		fields.add(nickNameField);

		// 备注字段
		FormField remarkField = new FormField();
		remarkField.setProp("remark");
		remarkField.setLabel("备注");
		remarkField.setType("input");
		remarkField.setPlaceholder("请输入备注信息");
		remarkField.setProps(Map.of("type", "textarea", "rows", 2, "size", "medium"));
		fields.add(remarkField);

		// 头像字段 (自定义类型)
		FormField avatarField = new FormField();
		avatarField.setProp("avatar");
		avatarField.setLabel("头像");
		avatarField.setType("urlImage");
		fields.add(avatarField);

		formConfig.setFields(fields);
		return formConfig;
	}

	/**
	 * 构建公司搜索表单配置
	 *
	 * @return 公司搜索表单配置
	 */
	private FormConfig buildCompanySearchFormConfig() {
		FormConfig formConfig = new FormConfig();
		formConfig.setExpandable(true);
		formConfig.setExpanded(true);
		formConfig.setGutter(20);
		formConfig.setLabelWidth("100px");

		List<FormField> fields = new ArrayList<>();

		// 公司ID字段
		FormField idField = new FormField();
		idField.setProp("equalsId");
		idField.setLabel("公司ID");
		idField.setType("input");
		idField.setPlaceholder("请输入公司ID");
		idField.setPrefixIcon("el-icon-search");
		fields.add(idField);

		// 公司名称字段
		FormField nameField = new FormField();
		nameField.setProp("likeName");
		nameField.setLabel("公司名称");
		nameField.setType("input");
		nameField.setPlaceholder("请输入公司名称");
		nameField.setPrefixIcon("el-icon-office-building");
		fields.add(nameField);

		// 税号字段
		FormField taxNumberField = new FormField();
		taxNumberField.setProp("equalsTaxNumber");
		taxNumberField.setLabel("税号");
		taxNumberField.setType("input");
		taxNumberField.setPlaceholder("请输入税号");
		taxNumberField.setPrefixIcon("el-icon-document");
		fields.add(taxNumberField);

		SelectFormField flagField = new SelectFormField();
		flagField.setProp("flagEquals");
		flagField.setLabel("公司状态");
		flagField.setOptions(
				List.of(SystemCompanyFlagEnum.ACTIVITED, SystemCompanyFlagEnum.SYSTEMED, SystemCompanyFlagEnum.DISABLED)
						.stream().map(flag -> {
							return new OptionValue<>(flag.getCode().toString(), flag.getValue());
						}).collect(Collectors.toList()));
		Map<String, Object> flagProps = new HashMap<>(3);
		flagProps.put("size", "medium");
		flagProps.put("multiple", true);
		flagField.setProps(flagProps);

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
}
