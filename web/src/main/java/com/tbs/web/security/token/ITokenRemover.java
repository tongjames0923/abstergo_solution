package com.tbs.web.security.token;

import org.jetbrains.annotations.NotNull;

public interface ITokenRemover {

    /**
     * 使令牌失效（标记为非活跃）
     * <p>
     * 将令牌状态置为失效，使其后续校验不通过；与 {@link #revokeToken} 的语义由实现方约定。
     * </p>
     *
     * @param token 待失效的令牌模型，不能为空
     */
    void inactiveToken( ITokenModel token);

    /**
     * 吊销令牌
     * <p>
     * 主动撤销令牌的可用性（如用户登出、权限变更），通常伴随缓存与存储的清理。
     * </p>
     *
     * @param token 待吊销的令牌模型，不能为空
     */
    void revokeToken( ITokenModel token);
}
