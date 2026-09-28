package com.tbs.app.security.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 第三方应用登录授权请求表单（{@code third_party} 授权类型）。
 * <p>
 * 承载第三方应用以「应用标识 + 授权码 + 时间戳」换取访问令牌所需的请求体，
 * 由统一登录门面依据 {@code getGrantType()} 反序列化为本强类型表单后传入 {@code requestToken}。
 * </p>
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DefaultThirdPartyLoginForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 授权码
     */
    private String code;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 时间戳
     */
    private Long timestamp;

}
