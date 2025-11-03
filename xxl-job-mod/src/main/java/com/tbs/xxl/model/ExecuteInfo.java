package com.tbs.xxl.model;

import lombok.Data;
import lombok.ToString;

import java.util.Map;

@Data
@ToString
public class ExecuteInfo {
    private String method;
    private Map params;
}
