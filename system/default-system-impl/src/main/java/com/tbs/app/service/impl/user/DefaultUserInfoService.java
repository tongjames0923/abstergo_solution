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
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.shared.pojo.entity.SystemUserExternalIdentityEntity;
import com.tbs.sql.service.QueryEngine;
import com.tbs.web.exception.SimpleAuthException;
import com.tbs.web.security.user.IUserInfoService;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.annotation.Resource;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tbs.app.vo.DepartmentInfoVO.convertToDepartmentVOs;
import com.tbs.app.constant.SystemI18nConstant;

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
        AssertUtil.isTrue(CollUtil.isEmpty(users), () -> new SimpleAuthException(I18nUtil.get(com.tbs.app.constant.SystemI18nConstant.USER_NOT_FOUND)));
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

    /**
     * 通过外部身份标识获取标准用户信息
     * <p>
     * 按外部身份绑定关系解析系统用户，再复用 {@link #loadUserInfoByUniqueKey(String)}
     * 的既有链路构建标准用户详情（角色、部门、状态语义完全一致）；
     * 未绑定或绑定用户不存在时返回 {@code null}，由调用方断言提示。
     * </p>
     *
     * @param identityType 外部身份类型（如 wecom）
     * @param externalId   外部身份标识（如企微 userid）
     * @return 绑定的标准用户详情；未绑定时返回 {@code null}
     */
    @Override
    public UserDetails loadUserInfoByExternalIdentity(String identityType, String externalId) {
        if (identityType == null || identityType.isEmpty() || externalId == null || externalId.isEmpty()) {
            return null;
        }
        SystemUserExternalIdentityEntity identity = QueryEngine
            .getEntityQueryWrapper(new SystemUserExternalIdentityEntity()).query().where()
            .equal(SystemUserExternalIdentityEntity::getIdentityType, identityType)
            .equal(SystemUserExternalIdentityEntity::getExternalId, externalId)
            .pour().fetchOne();
        if (identity == null) {
            return null;
        }
        SystemUserEntity user = systemUserService.queryById(identity.getUserId());
        if (user == null) {
            return null;
        }
        return loadUserInfoByUniqueKey(user.getUserName());
    }

}
