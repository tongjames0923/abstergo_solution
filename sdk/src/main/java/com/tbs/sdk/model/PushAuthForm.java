package com.tbs.sdk.model;

import java.util.Map;
import lombok.Data;

/**
 * 推送连接认证表单
 * <p>
 * 对应服务端PushAuthForm：token为登录令牌串（必填，由业务认证器校验），
 * userId与attributes可选（认证通过后注入会话属性）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushAuthForm {

    /**
     * 登录令牌串（必填）
     */
    private String token;

    /**
     * 用户ID（可选）
     */
    private String userId;

    /**
     * 会话属性（可选）
     */
    private Map<String, String> attributes;
}
