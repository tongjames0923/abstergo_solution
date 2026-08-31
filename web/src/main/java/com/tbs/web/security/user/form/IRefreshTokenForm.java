package com.tbs.web.security.user.form;

import com.tbs.web.security.token.ITokenModel;

import java.io.Serializable;

/**
 * 刷新令牌表单
 * <p>
 * 换发访问令牌时携带的凭证载体，当前仅封装一个刷新令牌模型。
 * </p>
 *
 * @author abstergo
 * @see ITokenModel 令牌模型
 */
public interface IRefreshTokenForm extends Serializable {

    /**
     * 获取刷新令牌
     *
     * @return 待使用的刷新令牌模型，不能为空
     */
    ITokenModel getRefreshToken();

}
