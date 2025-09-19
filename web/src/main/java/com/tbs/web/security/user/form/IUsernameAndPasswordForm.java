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
     * @return
     */
    String getUsername();

    /**
     * 获取密码
     *
     * @return
     */
    String getPassword();
}
