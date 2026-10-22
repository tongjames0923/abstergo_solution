package com.tbs.web.security.authentication.model;

import com.tbs.web.security.token.ITokenModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * token认证接入接口
 * <p>
 * 在 Spring Security {@link Authentication} 契约之上扩展令牌载体能力，使认证对象可直接持有
 * {@link ITokenModel} 与认证过程中的异常，便于拦截器与认证中心共享状态。
 * </p>
 *
 * @author tongj
 * @see Authentication Spring Security 认证契约
 */
public interface ITokenAuthentication extends Authentication {
    /**
     * 获取关联的令牌模型
     *
     * @return 令牌模型；未绑定时为 {@code null}
     */
    ITokenModel getTokenModel();

    /**
     * 设置关联的令牌模型
     *
     * @param model 待绑定的令牌模型，可以为 {@code null}
     */
    void setTokenModel(ITokenModel model);

    /**
     * 判断是否已绑定令牌
     *
     * @return 已绑定令牌返回 {@code true}，否则 {@code false}
     */
    default boolean hasToken() {
        return getTokenModel() != null;
    }

    /**
     * 获取认证关联的用户详情
     *
     * @return 用户详情，可能为 {@code null}（认证未完成时）
     */
    UserDetails getUserDetails();

    /**
     * 设置认证关联的用户详情
     * <p>
     * 由认证中心在认证阶段一次性注入。注入后本次认证对象即冻结为该用户详情的快照，
     * 同一请求内多次读取返回同一对象，不再回源，以满足 Spring Security 对
     * {@link Authentication} 作为认证结果快照的语义。
     * </p>
     *
     * @param userDetails 用户详情，可以为 {@code null}（认证失败时）
     */
    void setUserDetails(UserDetails userDetails);

    /**
     * 获取认证过程中产生的异常
     *
     * @return 认证异常；无异常时为 {@code null}
     */
    AuthenticationException getAuthException();

    /**
     * 记录认证过程中的异常
     * <p>
     * 用于在认证链路中传递失败原因，供后续处理器转换为对应响应。
     * </p>
     *
     * @param e 认证异常，可以为 {@code null}
     */
    void setAuthException(AuthenticationException e);
}
