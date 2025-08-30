package com.tbs.app.service.business.auth.impl


import com.tbs.app.service.business.role.IUserRoleRelationService
import com.tbs.app.service.business.user.ISystemUserService
import com.tbs.common.error.AssertUtil
import com.tbs.common.util.CollUtil
import com.tbs.shared.enums.SystemUserStatusEnum
import com.tbs.shared.pojo.entity.SystemUserEntity
import com.tbs.web.exception.SimpleAuthException
import com.tbs.web.security.user.IUserInfoService
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import java.util.function.Supplier
import java.util.stream.Collectors
import javax.annotation.Resource


class DefaultUserInfoService : IUserInfoService {
    @Resource
    private lateinit var systemUserService: ISystemUserService

    @Resource
    private lateinit var relationService: IUserRoleRelationService


    public override fun loadUserInfoByUniqueKey(uniqueKey: String): UserDetails {
        val users: MutableList<SystemUserEntity> = systemUserService.queryByUserName(uniqueKey)
        AssertUtil.isTrue(CollUtil.isEmpty(users), Supplier { SimpleAuthException("用户不存在") })
        val user = users.get(0)
        val roles: MutableList<String> = relationService.queryRoles(users.get(0).getId())
        return User.builder().username(user.getUserName()).password(user.getPassword())
            .authorities(
                roles.stream()
                    .map<SimpleGrantedAuthority?> { role: String? -> SimpleGrantedAuthority(role) }
                    .collect(Collectors.toList()))
            .accountLocked(SystemUserStatusEnum.LOCKED.getCode() == user.getFlag())
            .disabled(SystemUserStatusEnum.DISABLED.getCode() == user.getFlag()).build()
    }


}
