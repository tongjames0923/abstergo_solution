package com.tbs.app.form;

import lombok.Data;

import java.io.Serializable;

@Data
public class DicItemSearchForm implements Serializable {
    private static final long serialVersionUID = 1L;
    private String dicCode;
    private String label;
}
