package com.tbs.app.service.impl.identity;

import com.tbs.app.service.identity.ExternalUserProfile;
import com.tbs.app.service.identity.IExternalIdentityResolver;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import org.springframework.core.Ordered;

import javax.annotation.Resource;
import java.util.List;

/**
 * 约定式唯一标识匹配策略（默认自动关联策略）
 * <p>
 * 约定：第三方平台用户标识（如企微 userid）与系统内登录用户名一致时自动关联。
 * 作为兜底策略适用于全部平台（order 取最低优先级，特定平台的高优策略先行）；
 * 后续邮箱/手机号等匹配策略以更高优先级实现本接口即可接入策略链。
 * </p>
 *
 * @author abstergo
 * @see IExternalIdentityResolver 自动关联策略 SPI
 */
public class UniqueKeyMatchResolver implements IExternalIdentityResolver {

    @Resource
    private ISystemUserService systemUserService;

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public boolean supports(String identityType) {
        return true;
    }

    @Override
    public Long resolve(ExternalUserProfile profile) {
        if (profile == null || StrUtil.isEmpty(profile.getExternalId())) {
            return null;
        }
        List<SystemUserEntity> users = systemUserService.queryByUserName(profile.getExternalId());
        if (CollUtil.isEmpty(users)) {
            return null;
        }
        return users.get(0).getId();
    }
}
