package com.tbs.sdk.model;

import lombok.Data;

/**
 * 企业微信SSO登录表单
 * <p>
 * 对应后端WecomLoginForm：以企微OAuth授权码换票登录。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class WecomLoginForm {

    /**
     * 企微OAuth授权码（必填）
     */
    private String code;

    /**
     * OAuth state（可选）
     */
    private String state;
}
