package com.tbs.web.security.token;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 抽象的Token移除器
 *
 * @author abstergo
 */
public class DefaultTokenRemover implements ITokenRemover {

    @Resource
    ITokenIssuanceCenter tokenIssuanceCenter;

    /**
     * 验证令牌
     *
     * @param token 令牌
     */
    protected void validateToken(@NotNull ITokenIssuanceCenter tokenIssuanceCenter, @NotNull ITokenModel token) {
        AssertUtil.notNull(token, "令牌不能为空");
        AssertUtil.hasText(token.getUserName(), "用户名不能为空");
        AssertUtil.hasText(token.getToken(), "令牌内容不能为空");
        AssertUtil.isTrue(!Objects.equals(token.getStatus(), TokenStatusEnum.ACTIVE), "令牌已失效");
    }

    @Override
    public void inactiveToken(@NotNull ITokenModel token) {
        validateToken(tokenIssuanceCenter, token);
        tokenIssuanceCenter.setTokenStatus(token.getToken(), TokenStatusEnum.INACTIVE);
    }

    @Override
    public void revokeToken(@NotNull ITokenModel token) {
        validateToken(tokenIssuanceCenter, token);
        tokenIssuanceCenter.setTokenStatus(token.getToken(), TokenStatusEnum.REVOKE);
    }
}
