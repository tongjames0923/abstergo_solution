package com.tbs.push.api.auth;

import com.tbs.push.model.PushAuthForm;
import com.tbs.push.model.PushAuthResult;

/**
 * 连接认证器 SPI
 * <p>
 * HTTP POST 认证端点的校验决策点，认证逻辑归属业务方：业务方注册实现 Bean
 * 即接入真实鉴权（如校验登录令牌、注入用户属性）。协议层默认装配
 * {@code NoopAuthenticator}（仅格式校验放行），延续"协议层不内置具体鉴权"的边界。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IPushAuthenticator {

    /**
     * 执行认证
     *
     * @param form 客户端提交的认证表单
     * @return 认证结果（通过时携带会话属性，失败时携带失败原因）
     */
    PushAuthResult authenticate(PushAuthForm form);

}
