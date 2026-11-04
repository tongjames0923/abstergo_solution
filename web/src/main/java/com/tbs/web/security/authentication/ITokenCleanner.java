package com.tbs.web.security.authentication;

import com.tbs.web.security.token.ITokenModel;

public interface ITokenCleanner {

    /**
     * 获取清理器名称
     *
     * @return
     */
    String getName();

    /**
     * 令牌状态迁移后的维护事件回调
     * <p>
     * 由令牌颁发中心在令牌落库（签发或状态变更）后统一派发，业务实现据此失效
     * 自身持有的派生缓存（如会话快照、登录信息）；实现须幂等且容忍重复回调，
     * 同一令牌的多次状态迁移会多次触发。异常由派发方逐个隔离，不需自行吞噬。
     * </p>
     *
     * @param token 刚完成落库的令牌模型
     */
    void clean(ITokenModel token);
}
