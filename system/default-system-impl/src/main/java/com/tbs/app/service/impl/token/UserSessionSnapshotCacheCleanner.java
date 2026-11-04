package com.tbs.app.service.impl.token;

import com.tbs.web.security.authentication.ITokenCleanner;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserLoginInfoService;
import org.springframework.stereotype.Service;

/**
 * 用户会话快照缓存清理器
 * <p>
 * 令牌签发/失效/吊销都是「用户名维度会话快照」的过期事件：令牌模型自带 userName，
 * 直接据此清除会话列表与可用会话ID列表两个派生缓存键，无需反查库；
 * 快照键布局由 {@link IUserLoginInfoService#evictUserSessionSnapshot} 的缓存属主持有，
 * 本类只做令牌事件到失效动作的转换声明。事件由令牌颁发中心（AbstractTokenIssuanceCenter#saveModel）
 * 在任意令牌落库后统一派发：登出、刷新签发/换废、强下线、后台吊销、惰性过期修正均自动覆盖，
 * 新增令牌写路径无需任何登记。
 * </p>
 *
 * @author abstergo
 */
@Service
public class UserSessionSnapshotCacheCleanner implements ITokenCleanner {

    private final IUserLoginInfoService loginInfoService;

    public UserSessionSnapshotCacheCleanner(IUserLoginInfoService loginInfoService) {
        this.loginInfoService = loginInfoService;
    }

    @Override
    public String getName() {
        return "用户会话快照缓存清理器";
    }

    @Override
    public void clean(ITokenModel token) {
        if (token == null) {
            return;
        }
        loginInfoService.evictUserSessionSnapshot(token.getUserName());
    }
}
