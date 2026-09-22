package com.tbs.app.security.grant;

import com.tbs.app.form.LoginForm;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.access.IUsernamePasswordAccessTokenProvider;
import com.tbs.web.security.user.access.grant.ILoginGrantProvider;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * 获取可下发给前端的公开参数
     * <p>
     * 仅允许放置公开参数（如企微 corpId/agentId）；secret 等敏感配置禁止导出。
     * </p>
     *
     * @return 公开参数映射，无参数时返回空映射
     */
    @Override
    public Map<String, Object> getPublicOptions() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("username", "{USER_NAME：用户名}");
        stringObjectMap.put("password", "{PASSWORD：密码}");
        stringObjectMap.put("loginType", "0/null web登录，1客户端登录");
        return stringObjectMap;
    }
}
