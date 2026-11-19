package com.tbs.script.api.expressible.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnumValue implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 枚举值
     */
    private String value;

    /**
     * 枚举值标签
     */
    private String label;
}
