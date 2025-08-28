package com.tbs.shared.pojo.model.view;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 下拉选择表单字段类 用于表示下拉选择类型的表单字段
 */
@Setter
@Getter
public class SelectFormField extends FormField {
	/**
	 * 下拉选项列表 -- GETTER -- 获取下拉选项列表
	 *
	 *
	 * -- SETTER -- 设置下拉选项列表
	 *
	 * @return 下拉选项列表
	 * @param options 下拉选项列表
	 *
	 */
	private List<OptionValue<String>> options;

	/**
	 * 构造函数，设置字段类型为"select"
	 */
	public SelectFormField() {
		setType("select");
	}

}
