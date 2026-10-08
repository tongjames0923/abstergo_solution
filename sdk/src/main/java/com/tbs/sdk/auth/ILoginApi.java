package com.tbs.sdk.auth;

import com.tbs.sdk.model.LoginGrantModel;
import com.tbs.sdk.model.TokenModel;
import com.tbs.sdk.model.UserInfoModel;
import java.util.List;

/**
 * 登录流程契约
 * <p>
 * 封装服务端统一登录门面：按授权类型路由登录端点（POST /public/user/login/{grantType}），
 * 支持账号密码/App、企业微信SSO、第三方应用授权码等多种授权方式；成功后令牌自动写入
 * {@link TokenManager} 并由HTTP层注入后续请求。另提供用户信息、登录方式元数据查询与登出。
 * SDK不实现刷新令牌，会话失效（405/468）由调用方捕获后重新登录。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ILoginApi {

    /**
     * 通用授权入口
     * <p>
     * 调用POST /public/user/login/{grantType}，credential为各授权类型的凭证对象
     * （表单POJO或键值Map均可，由Jackson序列化），成功后令牌自动写入并注入。
     * </p>
     *
     * @param grantType  授权类型（如服务端注册的password/wecom/third_party等）
     * @param credential 凭证对象（可为null表示无body）
     * @return 登录令牌模型
     * @throws com.tbs.sdk.exception.SdkAuthException   登录被拒绝（凭证错误/方式禁用）时抛出
     * @throws com.tbs.sdk.exception.SdkServerException 服务端错误时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 网络失败、参数非法或响应非法时抛出
     */
    TokenModel login(String grantType, Object credential);

    /**
     * 账号密码登录（App模式：loginType=1）
     *
     * @param username 用户名（必填）
     * @param password 密码（必填）
     * @return 登录令牌模型（已写入令牌管理器）
     * @throws com.tbs.sdk.exception.SdkClientException 用户名/密码非法时抛出
     */
    TokenModel loginByPassword(String username, String password);

    /**
     * 企业微信SSO登录（企微OAuth授权码换票）
     *
     * @param code  企微OAuth授权码（必填）
     * @param state OAuth state（可选）
     * @return 登录令牌模型（已写入令牌管理器）
     * @throws com.tbs.sdk.exception.SdkClientException 授权码为空时抛出
     */
    TokenModel loginByWecom(String code, String state);

    /**
     * 第三方应用登录（以当前时间戳按约定算法生成授权码换票）
     *
     * @param appId     第三方应用标识
     * @param appSecret 第三方应用密钥
     * @return 登录令牌模型（已写入令牌管理器）
     */
    TokenModel loginByThirdParty(String appId, String appSecret);

    /**
     * 第三方应用登录（显式指定时间戳，服务端窗口±3分钟）
     *
     * @param appId     第三方应用标识
     * @param appSecret 第三方应用密钥
     * @param timestamp 毫秒时间戳（为null取当前时间）
     * @return 登录令牌模型（已写入令牌管理器）
     */
    TokenModel loginByThirdParty(String appId, String appSecret, Long timestamp);

    /**
     * 获取当前登录用户信息（GET /private/user/info，需登录令牌）
     *
     * @return 用户信息模型
     * @throws com.tbs.sdk.exception.SdkAuthException   令牌失效时抛出
     * @throws com.tbs.sdk.exception.SdkServerException 服务端错误时抛出
     */
    UserInfoModel getUserInfo();

    /**
     * 查询服务端启用的登录方式元数据（GET /public/user/login-grants）
     *
     * @return 启用的登录方式元数据列表
     * @throws com.tbs.sdk.exception.SdkServerException 服务端错误时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 网络失败或响应非法时抛出
     */
    List<LoginGrantModel> listEnabledGrants();

    /**
     * 判断是否已持有令牌
     *
     * @return 已登录返回true
     */
    boolean isLoggedIn();

    /**
     * 登出：尽力调用后端登出接口（GET /private/user/logout）注销令牌，
     * 无论后端是否成功均清理本地令牌（对齐前端logout兜底语义）。
     */
    void logout();
}
