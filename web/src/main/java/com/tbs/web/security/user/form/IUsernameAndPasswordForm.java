package com.tbs.web.security.user.form;

import java.io.Serializable;

/**
 * 用户名密码表单
 *
 * @author abstergo
 */
public interface IUsernameAndPasswordForm extends Serializable {
    /**
     * 获取用户名
     *
     * @return 登录用户名
     */
    String getUsername();

    /**
     * 获取密码
     *
     * @return 明文密码（将在 {@code AccessTokenProvider} 中加密比对）
     */
    String getPassword();

    /**
     * 登录类型
     * <p> 0/null 表示 Web 登录；1 表示客户端（应用）登录，对应签发不同的访问令牌类型。</p>
     */
    default Integer getLoginType() {
        return 0;
    }
}
