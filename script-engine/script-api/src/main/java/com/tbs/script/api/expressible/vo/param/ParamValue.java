package com.tbs.script.api.expressible.vo.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 参数值
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParamValue implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 参数类型
     */
    private String type;

    /**
     * 参数名称
     */
    private String name;

    /**
     * 参数排序
     */
    private Integer sort;

    /**
     * 参数值
     */
    private String value;
}
