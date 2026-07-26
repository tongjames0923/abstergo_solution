package com.tbs.web.genric.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * HTTP调用通用调用表单
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpInvokeForm implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 调用的接口id
     */
    private String id;
    /**
     * 调用的版本
     */
    private Integer version;
    /**
     * 调用的参数列表
     */
    List<GenricParameterForm> parameters;
}
