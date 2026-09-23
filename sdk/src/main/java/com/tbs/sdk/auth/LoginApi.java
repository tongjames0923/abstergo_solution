package com.tbs.sdk.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tbs.sdk.constants.SdkConstants;
import com.tbs.sdk.exception.SdkClientException;
import com.tbs.sdk.http.IBaseHttpClient;
import com.tbs.sdk.model.TokenModel;
import java.util.List;
import java.util.Map;

/**
 * 登录流程实现
 * <p>
 * 薄实现：仅做端点拼接、响应校验与令牌写入，网络与统一响应解析
 * 全部委托IBaseHttpClient，对应服务端"薄壳控制器→门面服务"的客户端映射。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class LoginApi implements ILoginApi {

    /**
     * 启用登录方式元数据列表类型引用
     */
    private static final TypeReference<List<Map<String, Object>>> GRANT_LIST_TYPE =
        new TypeReference<List<Map<String, Object>>>() {
        };

    /**
     * 统一HTTP客户端
     */
    private final IBaseHttpClient httpClient;

    /**
     * 令牌管理器
     */
    private final TokenManager tokenManager;

    /**
     * 构造登录API
     *
     * @param httpClient   统一HTTP客户端
     * @param tokenManager 令牌管理器
     */
    public LoginApi(IBaseHttpClient httpClient, TokenManager tokenManager) {
        this.httpClient = httpClient;
        this.tokenManager = tokenManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TokenModel login(String grantType, Map<String, Object> payload) {
        if (grantType == null || grantType.trim().isEmpty()) {
            throw new SdkClientException("grantType不能为空");
        }
        TokenModel tokenModel = httpClient.post(SdkConstants.PATH_LOGIN + grantType, payload, TokenModel.class);
        if (tokenModel == null || tokenModel.getToken() == null || tokenModel.getToken().trim().isEmpty()) {
            throw new SdkClientException("登录响应缺少有效令牌");
        }
        tokenManager.set(tokenModel);
        return tokenModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> listEnabledGrants() {
        return httpClient.get(SdkConstants.PATH_LOGIN_GRANTS, null, GRANT_LIST_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLoggedIn() {
        return tokenManager.hasToken();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logout() {
        tokenManager.clear();
    }
}
