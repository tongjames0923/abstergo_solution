package com.tbs.push.impl.auth;

import com.tbs.push.api.auth.IPushAuthenticator;
import com.tbs.push.model.auth.PushAuthForm;
import com.tbs.push.model.auth.PushAuthResult;

import java.util.Map;

/**
 * 空实现连接认证器（默认兜底）
 * <p>
 * 协议层默认不集成具体鉴权，本实现仅保留协议层的格式校验边界： 表单经协议层前置校验非空后直接放行，并把表单中的 userId 注入会话属性。 业务方注册自定义 IPushAuthenticator Bean
 * 即接入真实鉴权（校验令牌、注入角色等）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class NoopAuthenticator implements IPushAuthenticator {

    /**
     * 会话属性键：认证用户标识
     */
    public static final String ATTR_USER_ID = "userId";

    /**
     * 执行认证（默认放行并透传用户标识）
     *
     * @param form 客户端提交的认证表单
     * @return 认证通过结果（携带 userId 属性）
     */
    @Override
    public PushAuthResult authenticate(PushAuthForm form) {
        Map<String, String> attributes = new java.util.LinkedHashMap<>();
        if (form.getUserId() != null) {
            attributes.put(ATTR_USER_ID, form.getUserId());
        }
        if (form.getAttributes() != null) {
            attributes.putAll(form.getAttributes());
        }
        return PushAuthResult.success(attributes);
    }

}
