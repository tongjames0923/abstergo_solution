package com.tbs.app.service.business.user.impl;

import com.tbs.app.service.business.company.ICompanyUserRelationService;
import com.tbs.app.service.business.role.IUserRoleRelationService;
import com.tbs.app.service.business.user.ISystemUserService;
import com.tbs.app.service.business.user.model.impl.DefaultSystemUserDetails;
import com.tbs.cache.annotation.ExpireOn;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.shared.enums.SystemUserStatusEnum;
import com.tbs.web.exception.SimpleAuthException;
import com.tbs.web.security.user.IUserInfoService;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 默认用户信息服务
 */
public class DefaultUserInfoService implements IUserInfoService {

    @Resource
    private ISystemUserService systemUserService;
    @Resource
    private IUserRoleRelationService userRoleRelationService;
    @Resource
    private ICompanyUserRelationService companyUserRelationService;

    @Cacheable(cacheNames = USER_DETAILS_CACHE, key = "#uniqueKey")
    @ExpireOn(value = 5, timeUnit = TimeUnit.MINUTES)
    @Override
    public UserDetails loadUserInfoByUniqueKey(@NotNull String uniqueKey) {
        var users = systemUserService.queryByUserName(uniqueKey);
        AssertUtil.isTrue(CollUtil.isEmpty(users), () -> new SimpleAuthException("用户不存在"));
        var user = CollUtil.first(users);

        var builder = DefaultSystemUserDetails.builder().userName(user.getUserName()).password(user.getPassword())
            .accountNonLocked(!SystemUserStatusEnum.LOCKED.getCode().equals(user.getFlag())).accountNonExpired(true)
            .credentialsNonExpired(true).enabled(SystemUserStatusEnum.ACTIVITE.getCode().equals(user.getFlag()));
        var roles = userRoleRelationService.queryUserRoles(user.getId());
        if (CollUtil.isNotEmpty(roles)) {
            builder.authorities(roles.stream().map(role -> {
                return new SimpleGrantedAuthority(role.getId().toString());
            }).collect(Collectors.toList()));
        }
        var companies = companyUserRelationService.queryUserCompanies(user.getId());
        if (CollUtil.isNotEmpty(companies)) {
            builder.companies(companies);
        }
        return builder.build();
    }
}
