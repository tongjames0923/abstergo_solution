package com.tbs.app.form;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用版本查询表单
 * <p>
 * 用于版本列表的条件检索，所有字段均为可选条件，versionName 按模糊匹配处理
 *
 * @author abstergo
 */
@Data
public class AppVersionSearchForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 应用标识
     */
    private String appKey;

    /**
     * 外部版本名称，模糊匹配
     */
    private String versionName;

    /**
     * 版本类型
     *
     * @see com.tbs.shared.enums.AppVersionTypeEnum
     */
    private Integer versionType;

    /**
     * 平台，如 ANDROID、IOS、WEB
     */
    private String platform;

    /**
     * 生命周期状态
     *
     * @see com.tbs.shared.enums.AppVersionFlagEnum
     */
    private Integer flag;
}
