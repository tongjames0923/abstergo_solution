package com.tbs.web.security.issuance.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;

/**
 * 令牌请求表单
 * <p>
 * 封装签发令牌所需的全部要素：会话标识、用户详情、令牌类型与创建方式。
 * 由各类 {@code AccessTokenProvider} 组装后交给 {@code ITokenIssuanceCenter.issueToken} 签发。
 * </p>
 *
 * @see com.tbs.web.security.issuance.center.ITokenIssuanceCenter 令牌签发中心
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TokenRequestForm implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * sessionId
     */
    private  String sessionId;
    /**
     * 用户信息
     */
    private  UserDetails userDetails;
    /**
     * token类型
     * @see com.tbs.web.enums.TokenStatusEnum
     */
    private  Integer tokenType;
    /**
     * token创建方式
     * @see com.tbs.web.enums.TokenCreateTypeEnum
     */
    private  Integer tokenCreateType;
}
