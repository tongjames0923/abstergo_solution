package com.tbs.shared.pojo.model.view;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下拉框选项值 用于表示下拉选择框中的选项，包含值和标签两个属性
 *
 * @author tongj
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptionValue<V> implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 选项的值
	 */
	private V value;
	/**
	 * 选项的标签（显示文本）
	 */
	private String label;
}
