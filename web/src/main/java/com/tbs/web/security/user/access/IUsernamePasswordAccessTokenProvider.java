package com.tbs.web.security.user.access;

import com.tbs.web.security.user.form.IUsernameAndPasswordForm;

/**
 * 账号密码登录访问令牌提供者
 * <p>
 * 由「用户名 + 密码」表单换取访问令牌能力的契约，复用 {@link IUserAccessTokenProvider} 的泛型请求范式。
 * </p>
 * @author abstergo
 * @see IUserAccessTokenProvider 访问令牌提供者基类
 */
public interface IUsernamePasswordAccessTokenProvider extends IUserAccessTokenProvider<IUsernameAndPasswordForm> {
}
