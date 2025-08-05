package com.tbs.app.service.business.impl;

import com.tbs.app.constant.SystemAppBeanNameConstant;
import com.tbs.app.enums.SystemUserStatusEnum;
import com.tbs.app.pojo.entity.SystemRole;
import com.tbs.app.pojo.entity.SystemUser;
import com.tbs.app.service.business.IPasswordEncrypt;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.app.service.business.IUserRoleRelationService;
import com.tbs.cache.core.expire.IExpiredActuator;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.CollUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.exception.SimpleAuthException;
import com.tbs.web.security.IUserAuthService;
import com.tbs.web.security.model.SimpleTokenModel;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author abstergo
 */
public class SimpleUserAuthServiceImpl implements IUserAuthService {

    @Resource
    WebAuthProperty authProperty;

    @Resource
    ISystemUserService systemUserService;

    @Resource
    private IPasswordEncrypt passwordEncrypt;

    @Resource
    private IUserRoleRelationService relationService;

    @Resource(name = SystemAppBeanNameConstant.TOKEN_DATA_HOLDER)
    private IDataHolder tokenDataHolder;

    @Resource(name = SystemAppBeanNameConstant.TOKEN_EXPIRE_ACTUATOR)
    private IExpiredActuator<IDataHolder> tokenModelExpire;

    @Override
    public UserDetails loadUserInfoByUniqueKey(String uniqueKey) {
        List<SystemUser> users = systemUserService.queryByUserName(uniqueKey);
        AssertUtil.isTrue(CollUtil.isEmpty(users), () -> new SimpleAuthException("用户不存在"));
        SystemUser user = users.get(0);
        List<SystemRole> roles = relationService.queryUserRoles(users.get(0).getId());
        return User.builder().username(user.getUsername()).password(user.getPassword())
            .authorities(roles.stream().map(role -> {
                return new SimpleGrantedAuthority(role.getRole());
            }).collect(Collectors.toList())).accountLocked(SystemUserStatusEnum.LOCKED.getCode().equals(user.getFlag()))
            .disabled(SystemUserStatusEnum.DISABLED.getCode().equals(user.getFlag())).build();
    }

    @Override
    public UserDetails loadUserInfoByToken(com.tbs.web.security.ITokenModel token) {
        AssertUtil.notNull(token == null, () -> new InvalidTokenException("未接收到有效Token"));
        return (UserDetails)tokenDataHolder.get(token.getToken()).orElse(null);
    }

    @Override
    public com.tbs.web.security.ITokenModel login(String username, String password) {
        UserDetails details = loadUserInfoByUniqueKey(username);
        password = passwordEncrypt.encrypt(password);
        AssertUtil.isTrue(!Objects.equals(details.getPassword(), password), () -> {
            return new SimpleAuthException("用户名或密码错误");
        });
        checkUserDetails(details);
        var exp =
            new Date(System.currentTimeMillis() + Duration.ofSeconds(authProperty.getTokenExpireTime()).toMillis());
        com.tbs.web.security.ITokenModel model =
            SimpleTokenModel.create(UuidUtil.getInstance().getUuidString(), authProperty.getTokenSource(),
                authProperty.getTokenFiledName(), details.getUsername(), exp);
        boolean set = tokenDataHolder.set(model.getToken(), details, false);
        AssertUtil.isTrue(!set, () -> new FormattedRuntimeException("Token已存在"));

        tokenModelExpire.expire(tokenDataHolder, model.getToken(), exp);
        return model;
    }

    private void checkUserDetails(UserDetails details) {
        AssertUtil.notNull(details, () -> new SimpleAuthException("未获得有效的用户数据"));
        AssertUtil.isTrue(!details.isEnabled(), () -> new SimpleAuthException("用户被禁用"));

    }

    @Override
    public void authToken(com.tbs.web.security.ITokenModel iTokenModel) throws AuthenticationException {
        AssertUtil.notNull(iTokenModel, () -> new InvalidTokenException("未接收到有效Token"));
        AssertUtil.isTrue(StrUtil.isEmpty(iTokenModel.getToken()), () -> new InvalidTokenException("Token值为空"));
        AssertUtil.isTrue(
            iTokenModel.getExpireTime() != null && iTokenModel.getExpireTime().getTime() < System.currentTimeMillis(),
            () -> new InvalidTokenException("Token已过期"));
        UserDetails details = loadUserInfoByToken(iTokenModel);
        AssertUtil.isTrue(details == null, () -> new InvalidTokenException("Token已失效"));
        checkUserDetails(details);
    }

    @Override
    public boolean logout(com.tbs.web.security.ITokenModel iTokenModel) {
        AssertUtil.notNull(iTokenModel, () -> new InvalidTokenException("未接收到有效Token"));
        tokenDataHolder.remove(iTokenModel.getToken());
        return true;
    }
}
