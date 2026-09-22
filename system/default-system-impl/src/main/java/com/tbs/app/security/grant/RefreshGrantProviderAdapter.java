package com.tbs.app.security.grant;

import com.tbs.common.util.base.CollUtil;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.access.grant.ILoginGrantProvider;
import org.springframework.beans.factory.ObjectProvider;

public class RefreshGrantProviderAdapter implements ILoginGrantProvider<RefreshGrantForm> {

    ObjectProvider<IRefreshTokenService> refreshAccessTokenProviders;

    public RefreshGrantProviderAdapter(ObjectProvider<IRefreshTokenService> refreshAccessTokenProviders) {
        this.refreshAccessTokenProviders = refreshAccessTokenProviders;
    }

    public static final String Grand_Type = "refresh";

    /**
     * 获取授权类型唯一标识
     * <p>
     * 全局唯一，重复注册在启动期即失败；与前端登录方式 Provider id 对齐。
     * </p>
     *
     * @return 授权类型标识（如 password / wecom）
     */
    @Override
    public String getGrantType() {
        return Grand_Type;
    }

    /**
     * 获取凭证表单类型
     * <p>
     * 刷新令牌存于 Cookie，请求体无需字段，使用占位表单 {@link RefreshGrantForm}；
     * 空请求体可被门面正常反序列化为空表单，避免 {@code String} 等类型转换失败。
     * </p>
     *
     * @return 凭证表单 Class
     */
    @Override
    public Class<RefreshGrantForm> getFormType() {
        return RefreshGrantForm.class;
    }

    /**
     * 当前授权方式是否启用
     * <p>
     * 建议绑定各自配置总开关（{@code tbs.{module}.enable}）；未启用方式在统一端点与元数据导出中均被拒绝。
     * </p>
     *
     * @return 启用返回 {@code true}
     */
    @Override
    public boolean isEnabled() {
        return CollUtil.isNotEmpty(refreshAccessTokenProviders);
    }

    /**
     * 请求访问令牌
     * <p>
     * 刷新令牌从 {@code REFRESH_TOKEN} Cookie 读取，表单内容不使用；委托
     * {@link IRefreshTokenService#useRefreshToken()} 复用刷新令牌全链路（解析、校验、换发、一次性失效）。
     * </p>
     *
     * @param form 刷新授权占位表单，内容为空
     * @return 签发的访问令牌模型
     */
    @Override
    public ITokenModel requestToken(RefreshGrantForm form) {
        var r = CollUtil.getFirst(refreshAccessTokenProviders);
        return r.useRefreshToken();
    }
}
