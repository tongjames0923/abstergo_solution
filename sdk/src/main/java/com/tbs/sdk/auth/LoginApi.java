package com.tbs.sdk.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tbs.sdk.constants.SdkConstants;
import com.tbs.sdk.exception.SdkClientException;
import com.tbs.sdk.exception.SdkException;
import com.tbs.sdk.http.IBaseHttpClient;
import com.tbs.sdk.http.SdkRequestOptions;
import com.tbs.sdk.model.LoginForm;
import com.tbs.sdk.model.LoginGrantModel;
import com.tbs.sdk.model.ThirdPartyLoginForm;
import com.tbs.sdk.model.TokenModel;
import com.tbs.sdk.model.UserInfoModel;
import com.tbs.sdk.model.WecomLoginForm;
import java.util.List;

/**
 * 登录流程实现
 * <p>
 * 薄实现：仅做端点拼接、表单组装、响应校验与令牌写入，网络与统一响应解析
 * 全部委托{@link IBaseHttpClient}，对应服务端"薄壳控制器→门面服务"的客户端映射。
 * 各授权方式收敛到 {@link #login} 统一入口，密码/企微/第三方仅在入口前组装表单。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class LoginApi implements ILoginApi {

    /**
     * 启用登录方式元数据列表类型引用
     */
    private static final TypeReference<List<LoginGrantModel>> GRANT_LIST_TYPE =
        new TypeReference<List<LoginGrantModel>>() {
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
    public TokenModel login(String grantType, Object credential) {
        if (grantType == null || grantType.trim().isEmpty()) {
            throw new SdkClientException("grantType不能为空");
        }
        TokenModel tokenModel = httpClient.post(SdkConstants.PATH_LOGIN + grantType, credential, TokenModel.class);
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
    public TokenModel loginByPassword(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new SdkClientException("用户名不能为空");
        }
        if (password == null) {
            throw new SdkClientException("密码不能为空");
        }
        LoginForm form = new LoginForm();
        form.setUsername(username);
        form.setPassword(password);
        form.setLoginType(1);
        return login(SdkConstants.GRANT_PASSWORD, form);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TokenModel loginByWecom(String code, String state) {
        if (code == null || code.trim().isEmpty()) {
            throw new SdkClientException("企微授权码不能为空");
        }
        WecomLoginForm form = new WecomLoginForm();
        form.setCode(code);
        form.setState(state);
        return login(SdkConstants.GRANT_WECOM, form);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TokenModel loginByThirdParty(String appId, String appSecret) {
        return loginByThirdParty(appId, appSecret, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TokenModel loginByThirdParty(String appId, String appSecret, Long timestamp) {
        long ts = timestamp != null ? timestamp : System.currentTimeMillis();
        ThirdPartyLoginForm form = new ThirdPartyLoginForm();
        form.setAppId(appId);
        form.setTimestamp(ts);
        form.setCode(ThirdPartyAuthCode.generate(appId, appSecret, ts));
        return login(SdkConstants.GRANT_THIRD_PARTY, form);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserInfoModel getUserInfo() {
        return httpClient.get(SdkConstants.PATH_USER_INFO, null, UserInfoModel.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LoginGrantModel> listEnabledGrants() {
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
        try {
            // 隔离型请求：跳过统一登录态处理，令牌已失效也不触发重登事件，仅尽力通知后端
            httpClient.send("GET", SdkConstants.PATH_LOGOUT, null, null,
                new SdkRequestOptions().setSkipAuthHandling(true));
        } catch (SdkException ignored) {
            /*
             * 后端登出失败（网络异常/令牌已失效）不阻断本地清理，本地令牌始终清除
             */
        } finally {
            tokenManager.clear();
        }
    }
}
