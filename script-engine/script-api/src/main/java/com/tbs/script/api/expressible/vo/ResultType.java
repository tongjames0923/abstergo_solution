package com.tbs.script.api.expressible.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 结果类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultType implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 结果类型
     */
    private String type;
    /**
     * 结果名称
     */
    private String name;
    /**
     * 结果描述
     */
    private String description;
    /**
     * 结果排序
     */
    private Integer sort;
    /**
     * 是否是数组
     */
    private Boolean isArray;

}
