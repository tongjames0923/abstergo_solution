package com.tbs.app.service.getter;

import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.service.user.IUserIdentityService;
import com.tbs.app.service.user.model.UserIdentity;

import java.util.List;

/**
 * 按用户名解析任意用户身份快照的公共工具。
 *
 * <p>用户名由 getter 参数传入；为空时调用方按「当前登录用户」语义处理，
 * 非空时经用户名到用户 ID 的反查，再一次性装配权限与部门身份。</p>
 *
 * @author abstergo
 */
final class UserIdentityLookup {

    /**
     * 工具类不实例化
     */
    private UserIdentityLookup() {
    }

    /**
     * 按用户名解析用户身份。
     *
     * @param userService    系统用户服务
     * @param identityService 用户身份聚合服务
     * @param username       用户名；为空表示交由调用方按当前用户处理
     * @return 用户身份；用户不存在或取值异常时返回 null
     */
    static UserIdentity resolve(ISystemUserService userService, IUserIdentityService identityService,
        String username) {
        if (userService == null || identityService == null || username == null || username.isBlank()) {
            return null;
        }
        try {
            List<Long> userIds = userService.queryUserIdsByUserName(username.trim());
            if (userIds == null || userIds.isEmpty()) {
                return null;
            }
            return identityService.loadUserIdentity(userIds.get(0));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断用户身份是否处于可用状态。
     *
     * @param identity 用户身份
     * @return 启用且未锁定时返回 true
     */
    static boolean isActive(UserIdentity identity) {
        return identity != null && identity.isEnabled() && identity.isAccountNonLocked();
    }
}
