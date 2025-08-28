package com.tbs.shared.pojo.model.view;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下拉框选项值
 *
 * @author tongj
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptionValue<V> implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 值
	 */
	private V value;
	/**
	 * 标签
	 */
	private String label;
}
