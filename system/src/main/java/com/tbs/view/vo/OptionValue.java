package com.tbs.view.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 下拉框选项值
 *
 * @author tongj
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptionValue<V> implements Serializable {
    /**
     * 值
     */
    private V value;
    /**
     * 标签
     */
    private String label;
}
