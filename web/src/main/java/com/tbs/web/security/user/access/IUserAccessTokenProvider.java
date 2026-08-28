package com.tbs.web.security.user.access;

import com.tbs.web.security.token.ITokenModel;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * 用户访问令牌提供者
 * <p>
 * 以策略模式封装不同凭证形态（用户名密码、刷新令牌等）换取访问令牌的逻辑，
 * 通过泛型 {@code FORM} 适配各自的请求表单。
 * </p>
 *
 * @param <FORM> 请求表单类型，需可序列化
 * @author abstergo
 * @see ITokenModel 令牌模型
 */
public interface IUserAccessTokenProvider<FORM extends Serializable> {
    /**
     * 请求访问令牌
     * <p>
     * 按具体凭证完成认证并签发访问令牌；凭证非法或校验不通过时由实现方抛异常。
     * </p>
     *
     * @param form 换取令牌所需的请求表单，不能为空
     * @return 签发的访问令牌模型
     */
    ITokenModel requestToken( FORM form);

}
