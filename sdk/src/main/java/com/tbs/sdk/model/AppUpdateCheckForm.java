package com.tbs.sdk.model;

import lombok.Data;

/**
 * 应用升级检查表单
 * <p>
 * 对应服务端AppVersionCheckForm：公开接口入参，客户端上报应用标识、
 * 平台与当前内部版本号。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class AppUpdateCheckForm {

    /**
     * 应用标识
     */
    private String appKey;

    /**
     * 平台标识（如ANDROID、IOS、WEB）
     */
    private String platform;

    /**
     * 客户端当前内部版本号
     */
    private Long currentVersionCode;
}
