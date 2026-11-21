package com.tbs.script.api.expressible.vo.param;

import com.tbs.script.api.expressible.vo.EnumValue;
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
     * 参数展示标签，为空时前端回退使用 name
     */
    private String label;

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

    /**
     * 输入控件元信息，为空时前端按 type 推断控件
     */
    private ParamUiMeta ui;

}
