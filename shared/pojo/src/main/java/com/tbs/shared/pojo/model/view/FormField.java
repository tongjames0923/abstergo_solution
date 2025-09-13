package com.tbs.shared.pojo.model.view;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Getter;
import lombok.Setter;

/**
 * 表单字段基类 定义了表单字段的基本属性和类型信息，支持多种字段类型的序列化和反序列化
 */
@Setter
@Getter
public class FormField implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 字段属性名 -- GETTER -- 获取字段属性名
	 *
	 *
	 * -- SETTER -- 设置字段属性名
	 *
	 * @return 字段属性名
	 * @param prop 字段属性名
	 *
	 */
	private String prop;
	/**
	 * 字段标签 -- GETTER -- 获取字段标签
	 *
	 *
	 * -- SETTER -- 设置字段标签
	 *
	 * @return 字段标签
	 * @param label 字段标签
	 *
	 */
	private String label;
	/**
	 * 字段类型 -- GETTER -- 获取字段类型
	 *
	 *
	 * -- SETTER -- 设置字段类型
	 *
	 * @return 字段类型
	 * @param type 字段类型
	 *
	 */
	private String type;
	/**
	 * 占位符 -- GETTER -- 获取占位符
	 *
	 *
	 * -- SETTER -- 设置占位符
	 *
	 * @return 占位符
	 * @param placeholder 占位符
	 *
	 */
	private String placeholder;
	/**
	 * 是否可清除 -- GETTER -- 获取是否可清除
	 *
	 *
	 * -- SETTER -- 设置是否可清除
	 *
	 * @return 是否可清除
	 * @param clearable 是否可清除
	 *
	 */
	private Boolean clearable;
	/**
	 * 前缀图标 -- GETTER -- 获取前缀图标
	 *
	 *
	 * -- SETTER -- 设置前缀图标
	 *
	 * @return 前缀图标
	 * @param prefixIcon 前缀图标
	 *
	 */
	private String prefixIcon;

	/**
	 * 组件属性 -- GETTER -- 获取组件属性
	 *
	 *
	 * -- SETTER -- 获取组件属性
	 *
	 * @return 组件属性
	 * @param props 组件属性
	 *
	 */
	private Map<String, Object> props;

}
