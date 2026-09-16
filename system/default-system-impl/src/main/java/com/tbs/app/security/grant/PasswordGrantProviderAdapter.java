package com.tbs.app.security.grant;

import com.tbs.app.form.LoginForm;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.access.IUsernamePasswordAccessTokenProvider;
import com.tbs.web.security.user.access.grant.ILoginGrantProvider;

/**
 * 账号密码登录授权适配器
 * <p>
 * 将既有 {@link IUsernamePasswordAccessTokenProvider}（账密换令牌实现）桥接为 {@code password} 授权类型接入统一登录门面；既有实现零修改（铁律 11：契约只增不改）。
 * </p>
 *
 * @author abstergo
 * @see ILoginGrantProvider 登录授权提供者 SPI
 */
public class PasswordGrantProviderAdapter implements ILoginGrantProvider<LoginForm> {

    /**
     * 账号密码授权类型标识（与前端登录方式 Provider id 对齐）
     */
    public static final String GRANT_TYPE = "password";

    private final IUsernamePasswordAccessTokenProvider delegate;

    /**
     * 构建适配器
     *
     * @param delegate 既有账号密码访问令牌提供者
     */
    public PasswordGrantProviderAdapter(IUsernamePasswordAccessTokenProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getGrantType() {
        return GRANT_TYPE;
    }

    @Override
    public Class<LoginForm> getFormType() {
        return LoginForm.class;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public ITokenModel requestToken(LoginForm form) {
        return delegate.requestToken(form);
    }
}
