package com.tbs.app.form;

import lombok.Data;

import java.io.Serializable;

/**
 * 应用版本升级检查表单
 * <p>
 * 客户端上报自身的应用标识、平台与当前内部版本号，
 * 由服务端判定是否存在新版本及升级动作要求
 *
 * @author abstergo
 */
@Data
public class AppVersionCheckForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 应用标识
     */
    private String appKey;

    /**
     * 平台，如 ANDROID、IOS、WEB
     */
    private String platform;

    /**
     * 客户端当前内部版本号
     */
    private Long currentVersionCode;
}
