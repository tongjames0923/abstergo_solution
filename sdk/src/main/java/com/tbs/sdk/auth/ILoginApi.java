package com.tbs.sdk.auth;

import com.tbs.sdk.model.TokenModel;
import java.util.List;
import java.util.Map;

/**
 * 登录流程契约
 * <p>
 * 封装服务端统一登录门面：按授权类型路由登录端点，成功后令牌自动写入
 * TokenManager并由HTTP层注入后续请求。SDK不实现刷新令牌，
 * 认证异常（401/405/468）由调用方捕获后重新登录。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ILoginApi {

    /**
     * 统一登录
     * <p>
     * 调用POST /public/user/login/{grantType}，payload为各授权类型的
     * 自定义表单（如账号密码登录传用户名/密码键值），成功后令牌自动注入。
     * </p>
     *
     * @param grantType 授权类型（如服务端注册的password/wecom等）
     * @param payload   授权类型对应的表单键值（可为null表示无body）
     * @return 登录令牌模型
     * @throws com.tbs.sdk.exception.SdkAuthException   登录被拒绝（凭证错误/方式禁用）时抛出
     * @throws com.tbs.sdk.exception.SdkServerException 服务端错误时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 网络失败或响应非法时抛出
     */
    TokenModel login(String grantType, Map<String, Object> payload);

    /**
     * 查询服务端启用的登录方式元数据
     *
     * @return 启用的登录方式元数据列表（每项为grantType与公开选项键值）
     * @throws com.tbs.sdk.exception.SdkServerException 服务端错误时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 网络失败或响应非法时抛出
     */
    List<Map<String, Object>> listEnabledGrants();

    /**
     * 判断是否已持有令牌
     *
     * @return 已登录返回true
     */
    boolean isLoggedIn();

    /**
     * 登出（清除本地令牌；服务端侧令牌失效需另行调用对应接口）
     */
    void logout();
}
