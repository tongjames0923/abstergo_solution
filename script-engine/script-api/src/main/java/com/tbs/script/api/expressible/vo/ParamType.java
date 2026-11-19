package com.tbs.script.api.expressible.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParamType implements Serializable {
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
     * 参数描述
     */
    private String description;

    /**
     * 参数排序
     */
    private Integer sort;

    /**
     * 是否必需
     */
    private Boolean required;

    /**
     * 是否是枚举
     */
    private Boolean isEnum;

    /**
     * 枚举值
     */
    private Collection<EnumValue> enumValues;

}
