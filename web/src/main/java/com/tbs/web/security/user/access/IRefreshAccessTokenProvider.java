package com.tbs.web.security.user.access;

import com.tbs.web.security.user.form.IRefreshTokenForm;

/**
 * 刷新令牌提供者
 * <p>
 * 由刷新令牌表单换取访问令牌能力的契约，复用 {@link IUserAccessTokenProvider} 的泛型请求范式。
 * </p>
 *
 * @author abstergo
 * @see IUserAccessTokenProvider 访问令牌提供者基类
 */
public interface IRefreshAccessTokenProvider extends IUserAccessTokenProvider<IRefreshTokenForm> {
}
