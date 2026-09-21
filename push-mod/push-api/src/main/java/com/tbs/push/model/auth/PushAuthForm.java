package com.tbs.push.model.auth;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 连接认证请求表单
 * <p>
 * 客户端调用 HTTP POST 认证端点提交的业务凭证载体： 协议层不解释凭证内容，仅经{@code IPushAuthenticator} SPI 转交业务方校验， 校验通过后服务端签发预分配 sessionId
 * 与一次性连接凭证。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PushAuthForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务认证凭证（如登录令牌），类型化为{@link PushAuthToken}值对象（非空约束由类型承载）， 内容由业务认证器解释
     */
    private PushAuthToken token;

    /**
     * 业务用户标识（可选，供业务认证器使用与审计）
     */
    private String userId;

    /**
     * 扩展属性，认证通过后随凭证下发并注入会话 attributes
     */
    private Map<String, String> attributes;

}
