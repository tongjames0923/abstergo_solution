package com.tbs.web.security.user.form;

import com.tbs.web.security.token.ITokenModel;

import java.io.Serializable;

/**
 * 刷新令牌表单
 *
 * @author abstergo
 */
public interface IRefreshTokenForm extends Serializable {

    /**
     * 获取刷新令牌
     *
     * @return 刷新令牌
     */
    ITokenModel getRefreshToken();

}
