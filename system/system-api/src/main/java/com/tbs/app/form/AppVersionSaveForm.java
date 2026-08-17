package com.tbs.app.form;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用版本保存表单
 * <p>
 * 用于新建或编辑应用版本，id 有值时表示编辑既有版本，无值时表示新建（初始为草稿状态）
 *
 * @author abstergo
 */
@Data
public class AppVersionSaveForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 版本ID，编辑时必传
     */
    private Long id;

    /**
     * 应用标识
     */
    private String appKey;

    /**
     * 内部版本号
     */
    private Long versionCode;

    /**
     * 外部版本名称
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
     * 更新日志
     */
    private String releaseNote;

    /**
     * 构建信息
     */
    private String buildInfo;
}
