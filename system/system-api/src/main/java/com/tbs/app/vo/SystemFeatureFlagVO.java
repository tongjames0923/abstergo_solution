package com.tbs.app.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 系统功能标志
 *
 * @author tongj
 */
@Data
public class SystemFeatureFlagVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 功能标志名称
     */
    private String flagName;
    /**
     * 功能标志值
     */
    private String flagValue;
    /**
     * 功能标志描述
     */
    private String flagDesc;
}
