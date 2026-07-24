package com.tbs.web.genric.form;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * HTTP调用通用调用表单
 *
 * @author abstergo
 */
@Data
public class HttpInvokeForm implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private Integer version;
    List<GenricParameterForm> parameters;
}
