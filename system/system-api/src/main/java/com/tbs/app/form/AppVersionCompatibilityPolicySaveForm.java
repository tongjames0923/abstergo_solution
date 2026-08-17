package com.tbs.app.form;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用版本兼容策略保存表单
 * <p>
 * 用于新建或编辑跨应用兼容策略：源应用（source）发布版本后对目标应用（target）
 * 提出最低版本要求，id 有值时表示编辑既有策略
 *
 * @author abstergo
 */
@Data
public class AppVersionCompatibilityPolicySaveForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 策略ID，编辑时必传
     */
    private Long id;

    /**
     * 源应用标识，表示哪个应用发布的版本触发了该策略
     */
    private String sourceAppKey;

    /**
     * 源应用内部版本号，为空表示该源应用所有版本均适用此策略
     */
    private Long sourceVersionCode;

    /**
     * 源平台，如 ANDROID、IOS、WEB、BACKEND
     */
    private String sourcePlatform;

    /**
     * 目标应用标识，表示对哪个应用提出要求
     */
    private String targetAppKey;

    /**
     * 目标平台，如 ANDROID、IOS、WEB
     */
    private String targetPlatform;

    /**
     * 目标应用必须满足的最低内部版本号
     */
    private Long minCompatVersion;

    /**
     * 动作类型
     *
     * @see com.tbs.shared.enums.AppVersionCompatibilityActionEnum
     */
    private Integer action;
}
