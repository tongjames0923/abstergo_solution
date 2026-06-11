package com.tbs.app.service.impl.user;

import com.tbs.app.service.department.IDepartmentUserRelationService;
import com.tbs.app.service.role.IUserRoleRelationService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.service.user.model.impl.DefaultGrantedAuthority;
import com.tbs.app.service.user.model.impl.DefaultSystemUserDetails;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.shared.enums.SystemUserStatusEnum;
import com.tbs.shared.mapper.SystemCompanyMapper;
import com.tbs.web.exception.SimpleAuthException;
import com.tbs.web.security.user.IUserInfoService;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tbs.app.vo.DepartmentInfoVO.convertToDepartmentVOs;

/**
 * 默认用户信息服务
 */
public class DefaultUserInfoService implements IUserInfoService {

    @Resource
    private ISystemUserService systemUserService;
    @Resource
    private IUserRoleRelationService userRoleRelationService;
    @Resource
    private IDepartmentUserRelationService companyUserRelationService;
    @Resource
    private IDepartmentUserRelationService departmentUserRelationService;
    @Resource
    private SystemCompanyMapper companyMapper;

    @Override
    @Cacheable(cacheNames = IUserInfoService.USER_DETAILS_CACHE_NAME, key = "#uniqueKey")
    @ExpireOn(value = 5, timeUnit = TimeUnit.MINUTES)
    public UserDetails loadUserInfoByUniqueKey(@NotNull String uniqueKey) {
        var users = systemUserService.queryByUserName(uniqueKey);
        AssertUtil.isTrue(CollUtil.isEmpty(users), () -> new SimpleAuthException("用户不存在"));
        var user = CollUtil.of(users).findFirstOrNull();

        var builder = DefaultSystemUserDetails.builder().userName(user.getUserName()).password(user.getPassword())
            .accountNonLocked(!SystemUserStatusEnum.LOCKED.getCode().equals(user.getFlag())).accountNonExpired(true)
            .credentialsNonExpired(true).enabled(SystemUserStatusEnum.ACTIVITE.getCode().equals(user.getFlag()));
        var roles = userRoleRelationService.queryUserRoles(user.getId());
        if (CollUtil.isNotEmpty(roles)) {
            builder.authorities(roles.stream().map(role -> {
                return new DefaultGrantedAuthority(role.getRole());
            }).collect(Collectors.toList()));
        }
        var departments = departmentUserRelationService.queryUserDepartments(user.getId());
        if (CollUtil.isNotEmpty(departments)) {
            builder.departments(convertToDepartmentVOs(departments));
        }
        return builder.build();
    }

}
