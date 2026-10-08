package com.tbs.sdk.model;

import lombok.Data;

/**
 * 账号密码登录表单
 * <p>
 * 对应后端LoginForm：SDK默认loginType=1（客户端/App登录），0或缺省为Web登录。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class LoginForm {

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 登录模式：0/缺省=Web；1=客户端/App（SDK默认取1）
     */
    private Integer loginType = 1;
}
