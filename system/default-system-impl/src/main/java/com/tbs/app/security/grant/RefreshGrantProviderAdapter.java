package com.tbs.app.security.grant;

import com.tbs.common.util.base.CollUtil;
import com.tbs.web.security.token.IRefreshTokenService;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.access.grant.ILoginGrantProvider;
import org.springframework.beans.factory.ObjectProvider;

public class RefreshGrantProviderAdapter implements ILoginGrantProvider<String> {

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
     * 供统一门面将原始请求体反序列化为强类型表单。
     * </p>
     *
     * @return 凭证表单 Class
     */
    @Override
    public Class<String> getFormType() {
        return String.class;
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
     * 按具体凭证完成认证并签发访问令牌；凭证非法或校验不通过时由实现方抛异常。
     * </p>
     *
     * @param s 换取令牌所需的请求表单，不能为空
     * @return 签发的访问令牌模型
     */
    @Override
    public ITokenModel requestToken(String s) {
        var r = CollUtil.getFirst(refreshAccessTokenProviders);
        return r.useRefreshToken();
    }
}
