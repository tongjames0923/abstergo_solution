package com.tbs.app.service.business.impl;

import com.tbs.app.constant.SystemAppBeanNameConstant;
import com.tbs.app.enums.SystemUserStatusEnum;
import com.tbs.app.pojo.entity.SystemRole;
import com.tbs.app.pojo.entity.SystemUser;
import com.tbs.app.service.business.IPasswordEncrypt;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.app.service.business.IUserRoleRelationService;
import com.tbs.cache.core.IExpiredActuator;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.CollUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.schedule.ISchedule;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.exception.InvalidTokenException;
import com.tbs.web.exception.SimpleAuthException;
import com.tbs.web.security.ITokenModel;
import com.tbs.web.security.IUserAuthService;
import com.tbs.web.security.model.SimpleTokenModel;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.sql.Time;
import java.time.Duration;
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
        AssertUtil.isTrue(SystemUserStatusEnum.DISABLED.getCode().equals(user.getFlag()),
            () -> new SimpleAuthException("用户被禁用"));
        AssertUtil.isTrue(SystemUserStatusEnum.CREATED.getCode().equals(user.getFlag()),
            () -> new SimpleAuthException("用户未激活"));
        List<SystemRole> roles = relationService.queryUserRoles(users.get(0).getId());
        return User.builder().username(user.getUsername()).password(users.get(0).getPassword())
            .authorities(roles.stream().map(role -> {
                return new SimpleGrantedAuthority(role.getRole());
            }).collect(Collectors.toList())).build();
    }

    @Override
    public UserDetails loadUserInfoByToken(com.tbs.web.security.ITokenModel token) {
        AssertUtil.isTrue(token == null, () -> new InvalidTokenException("未接收到有效Token"));
        return (UserDetails)tokenDataHolder.get(token.getToken()).orElse(null);
    }

    @Override
    public com.tbs.web.security.ITokenModel login(String username, String password) {
        UserDetails details = loadUserInfoByUniqueKey(username);
        password = passwordEncrypt.encrypt(password);
        AssertUtil.isTrue(!Objects.equals(details.getPassword(), password), () -> {
            return new SimpleAuthException("用户名或密码错误");
        });
        com.tbs.web.security.ITokenModel model =
            SimpleTokenModel.create(UuidUtil.getInstance().getUuidString(), authProperty.getTokenSource(),
                authProperty.getTokenFiledName(), details.getUsername(), null);
        boolean set = tokenDataHolder.set(model.getToken(), details, false);
        AssertUtil.isTrue(!set, () -> new FormattedRuntimeException("Token已存在"));

        tokenModelExpire.expire(tokenDataHolder, model.getToken(),
            new Time(System.currentTimeMillis() + Duration.ofSeconds(authProperty.getTokenExpireTime()).toMillis()));
        return model;

    }

    @Override
    public void authToken(com.tbs.web.security.ITokenModel iTokenModel) throws AuthenticationException {
        if (StrUtil.isNotEmpty(iTokenModel.getToken())) {
            if (loadUserInfoByToken(iTokenModel) != null) {
                return;
            }
        }
        AssertUtil.throwError(() -> new InvalidTokenException("Token不存在有效用户信息"));
    }

    @Override
    public boolean logout(com.tbs.web.security.ITokenModel iTokenModel) {
        AssertUtil.notNull(iTokenModel, () -> new InvalidTokenException("未接收到有效Token"));
        tokenDataHolder.remove(iTokenModel.getToken());
        return true;
    }
}
