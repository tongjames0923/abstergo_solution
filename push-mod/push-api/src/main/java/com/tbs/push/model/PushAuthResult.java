package com.tbs.push.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 连接认证结果
 * <p>
 * {@code IPushAuthenticator} 的产出：携带会话维度认证属性（用户标识、角色等），
 * 由凭证服务注入{@code PushCredential}并最终写入连接会话 attributes，
 * 供投递协调器与拦截器消费。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushAuthResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 认证是否通过
     */
    private boolean authenticated;

    /**
     * 认证失败原因（认证器返回失败时填充，用于 i18n 参数）
     */
    private String failureReason;

    /**
     * 认证通过后的会话属性（如 userId、角色等），注入会话 attributes
     */
    private Map<String, String> attributes;

    /**
     * 构造认证通过结果
     *
     * @param attributes 会话属性
     * @return 认证通过结果
     */
    public static PushAuthResult success(Map<String, String> attributes) {
        PushAuthResult result = new PushAuthResult();
        result.setAuthenticated(true);
        result.setAttributes(attributes);
        return result;
    }

    /**
     * 构造认证失败结果
     *
     * @param failureReason 失败原因
     * @return 认证失败结果
     */
    public static PushAuthResult failure(String failureReason) {
        PushAuthResult result = new PushAuthResult();
        result.setAuthenticated(false);
        result.setFailureReason(failureReason);
        return result;
    }

}
