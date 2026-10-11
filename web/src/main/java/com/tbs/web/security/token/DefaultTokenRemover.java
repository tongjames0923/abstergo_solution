package com.tbs.web.security.token;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import org.jetbrains.annotations.NotNull;

import jakarta.annotation.Resource;
import java.util.Objects;

/**
 * 默认的令牌移除器
 * <p>
 * 通过 {@link ITokenIssuanceCenter#setTokenStatus} 将令牌置为失效状态，实现令牌的吊销/禁用。
 * 失效前做基础校验，确保只针对有效且归属完整的令牌操作。
 * </p>
 *
 * @author abstergo
 * @see ITokenRemover 令牌移除契约
 */
public class DefaultTokenRemover implements ITokenRemover {

    @Resource
    ITokenIssuanceCenter tokenIssuanceCenter;

    /**
     * 令牌失效前的基础校验
     *
     * @param token 待操作的令牌模型，不能为空
     */
    protected void validateToken( ITokenIssuanceCenter tokenIssuanceCenter,  ITokenModel token) {
        AssertUtil.notNull(token, "令牌不能为空");
        AssertUtil.hasText(token.getUserName(), "用户名不能为空");
        AssertUtil.hasText(token.getToken(), "令牌内容不能为空");
        AssertUtil.isTrue(!Objects.equals(token.getStatus(), TokenStatusEnum.ACTIVE), "令牌已失效");
    }

    @Override
    public void inactiveToken( ITokenModel token) {
        validateToken(tokenIssuanceCenter, token);
        // 置为非活跃状态（INACTIVE）
        tokenIssuanceCenter.setTokenStatus(token.getToken(), TokenStatusEnum.INACTIVE);
    }

    @Override
    public void revokeToken( ITokenModel token) {
        validateToken(tokenIssuanceCenter, token);
        // 置为已吊销状态（REVOKE），语义上比 INACTIVE 更强，通常不可恢复
        tokenIssuanceCenter.setTokenStatus(token.getToken(), TokenStatusEnum.REVOKE);
    }
}
