package com.tbs.sdk.model;

import lombok.Data;

/**
 * 第三方应用登录表单
 * <p>
 * 对应后端DefaultThirdPartyLoginForm：code由{@link com.tbs.sdk.auth.ThirdPartyAuthCode}
 * 按约定算法生成，timestamp有效窗口±3分钟。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class ThirdPartyLoginForm {

    /**
     * 授权码（必填，按约定算法生成）
     */
    private String code;

    /**
     * 第三方应用标识（必填）
     */
    private String appId;

    /**
     * 毫秒时间戳（必填，±3分钟窗口）
     */
    private Long timestamp;
}
