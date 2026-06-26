package com.tbs.app.form;

import lombok.Data;

import java.io.Serializable;

@Data
public class DicSearchForm implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 字典编码
     */
    private String dicCode;
    /**
     * 字典名称
     */
    private String dicName;
}
