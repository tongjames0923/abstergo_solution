package com.tbs.web.genric.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 泛化接口参数表单
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenricParameterForm implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 参数名称
     */
    private String parameterName;

    /**
     * 期望的参数类型
     */
    private String exceptdClassName;

    /**
     * 参数值
     */
    private Serializable parameterValue;

    /**
     * 排序
     */
    private Integer sort;

}
