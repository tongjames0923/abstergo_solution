package com.tbs.app.security.form;

import lombok.Data;

import java.io.Serializable;

/**
 * 企业微信登录凭证表单
 * <p>
 * 承载企业微信 OAuth 授权码（重定向与内嵌扫码两种前端交互均产出 code， 后端统一按授权码换取用户身份）。
 * </p>
 *
 * @author abstergo
 */
@Data

public class WecomLoginForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 企业微信 OAuth 授权码（一次性）
     */
    private String code;

    /**
     * 授权回传状态标识（原样携带的 state，用于发起方核对，可不传）
     */
    private String state;
}
