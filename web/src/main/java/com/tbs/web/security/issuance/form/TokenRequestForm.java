package com.tbs.web.security.issuance.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TokenRequestForm implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * sessionId
     */
    private @NotNull String sessionId;
    /**
     * 用户信息
     */
    private @NotNull UserDetails userDetails;
    /**
     * token类型
     */
    private @NotNull Integer tokenType;
    /**
     * token创建方式
     */
    private @NotNull Integer tokenCreateType;
}
