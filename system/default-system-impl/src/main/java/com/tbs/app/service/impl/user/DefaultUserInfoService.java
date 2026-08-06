package com.tbs.app.service.impl.user;

import com.tbs.app.service.department.IDepartmentUserRelationService;
import com.tbs.app.service.role.ISystemRoleGroupService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.service.user.model.impl.DefaultGrantedAuthority;
import com.tbs.app.service.user.model.impl.DefaultSystemUserDetails;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.shared.enums.SystemUserStatusEnum;
import com.tbs.shared.mapper.SystemCompanyMapper;
import com.tbs.web.exception.SimpleAuthException;
import com.tbs.web.security.user.IUserInfoService;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tbs.app.vo.DepartmentInfoVO.convertToDepartmentVOs;

/**
 * 默认用户信息服务
 * @author abstergo
 */
public class DefaultUserInfoService implements IUserInfoService {

    @Resource
    private ISystemUserService systemUserService;
    @Resource
    private ISystemRoleGroupService userRoleRelationService;
    @Resource
    private IDepartmentUserRelationService companyUserRelationService;
    @Resource
    private IDepartmentUserRelationService departmentUserRelationService;
    @Resource
    private SystemCompanyMapper companyMapper;

    @Override
    public UserDetails loadUserInfoByUniqueKey( String uniqueKey) {
        var users = systemUserService.queryByUserName(uniqueKey);
        AssertUtil.isTrue(CollUtil.isEmpty(users), () -> new SimpleAuthException(I18nUtil.get("user.not.found")));
        var user = CollUtil.of(users).findFirstOrNull();

        var builder = DefaultSystemUserDetails.builder().userName(user.getUserName()).password(user.getPassword())
            .accountNonExpired(true).credentialsNonExpired(true);
        if (Objects.equals(SystemUserStatusEnum.ACTIVE.getCode(), user.getFlag())) {
            builder.accountNonLocked(true).enabled(true);
        } else if (Objects.equals(SystemUserStatusEnum.LOCKED.getCode(), user.getFlag())) {
            builder.accountNonLocked(false).enabled(true);
        } else if (Objects.equals(SystemUserStatusEnum.INACTIVE.getCode(), user.getFlag())) {
            builder.accountNonLocked(true).enabled(false);
        }

        var roles = userRoleRelationService.getUserRoleGroupVOList(user.getId());
        if (CollUtil.isNotEmpty(roles)) {
            builder.authorities(roles.stream().map(role -> {
                return new DefaultGrantedAuthority(role);
            }).collect(Collectors.toList()));
        }
        var departments = departmentUserRelationService.queryUserDepartments(user.getId());
        if (CollUtil.isNotEmpty(departments)) {
            builder.departments(convertToDepartmentVOs(departments));
        }
        return builder.build();
    }

}
