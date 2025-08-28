package com.tbs.shared.pojo.model.view;

import lombok.Getter;
import lombok.Setter;

/**
 * 动态选项表单字段类
 * 用于表示通过API动态加载选项的表单字段
 */
@Setter
@Getter
public class DynamicOptionsFormField extends FormField {
	/**
	 * API接口地址
     * -- GETTER --
     *  获取API接口地址
     *
     *
     * -- SETTER --
     *  设置API接口地址
     *
     @return API接口地址
      * @param apiUrl API接口地址

     */
	private String apiUrl;

	/**
	 * 构造函数，设置字段类型为"dynamic-options"
	 */
	public DynamicOptionsFormField() {
		setType("dynamic-options");
	}

}
