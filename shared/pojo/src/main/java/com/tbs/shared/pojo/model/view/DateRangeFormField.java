package com.tbs.shared.pojo.model.view;

import lombok.Getter;
import lombok.Setter;

/**
 * 日期范围表单字段类 用于表示日期范围选择器类型的表单字段
 */
@Setter
@Getter
public class DateRangeFormField extends FormField {
	/**
	 * 日期值格式 -- GETTER -- 获取日期值格式
	 *
	 *
	 * -- SETTER -- 设置日期值格式
	 *
	 * @return 日期值格式
	 * @param valueFormat 日期值格式
	 *
	 */
	private String valueFormat;
	/**
	 * 显示格式 -- GETTER -- 获取显示格式
	 *
	 *
	 * -- SETTER -- 设置显示格式
	 *
	 * @return 显示格式
	 * @param format 显示格式
	 *
	 */
	private String format;
	/**
	 * 开始时间占位符 -- GETTER -- 获取开始时间占位符
	 *
	 *
	 * -- SETTER -- 设置开始时间占位符
	 *
	 * @return 开始时间占位符
	 * @param startPlaceholder 开始时间占位符
	 *
	 */
	private String startPlaceholder;
	/**
	 * 结束时间占位符 -- GETTER -- 获取结束时间占位符
	 *
	 *
	 * -- SETTER -- 设置结束时间占位符
	 *
	 * @return 结束时间占位符
	 * @param endPlaceholder 结束时间占位符
	 *
	 */
	private String endPlaceholder;
	/**
	 * 范围分隔符 -- GETTER -- 获取范围分隔符
	 *
	 *
	 * -- SETTER -- 设置范围分隔符
	 *
	 * @return 范围分隔符
	 * @param rangeSeparator 范围分隔符
	 *
	 */
	private String rangeSeparator;

	/**
	 * 构造函数，设置字段类型为"date-range"
	 */
	public DateRangeFormField() {
		setType("date-range");
	}

}
