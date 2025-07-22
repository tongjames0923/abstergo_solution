package com.tbs.app.service.business.impl;

import com.tbs.app.constant.SystemAppConstant;
import com.tbs.app.pojo.entity.SystemUser;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.common.holder.context.IContext;
import com.tbs.common.util.*;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.exception.SimpleAuthException;
import com.tbs.web.security.IUserAuthService;
import com.tbs.web.security.model.SimpleTokenModel;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author abstergo
 */
public class SimpleUserAuthServiceImpl implements IUserAuthService {

    @Resource
    WebAuthProperty authProperty;

    @Resource
    ISystemUserService systemUserService;

    /**
     * token上下文
     */
    IContext<UserDetails> tokenModelContext;

    protected IContext<UserDetails> getTokenModelContext() {
        if (tokenModelContext == null) {
            tokenModelContext = HolderUtil.getContext(SystemAppConstant.TOKEN_CONTEXT_ID);
        }
        return tokenModelContext;
    }


    @Override
    public UserDetails loadUserInfoByUniqueKey(String uniqueKey) {

        List<SystemUser> users = systemUserService.queryByUserName(uniqueKey);
        AssertUtil.isTrue(CollUtil.isEmpty(users), () -> new SimpleAuthException("用户不存在"));
        //FIXME 用户权限类待实现
        return User.builder().username(users.get(0).getUsername()).password(users.get(0).getPassword())
            .authorities("USER").build();
    }

    @Override
    public UserDetails loadUserInfoByToken(com.tbs.web.security.ITokenModel token) {
        AssertUtil.isTrue(token == null, () -> new SimpleAuthException("Token错误"));
        return HolderUtil.get(getTokenModelContext(), token.getToken());
    }

    @Override
    public com.tbs.web.security.ITokenModel login(String username, String password) {
        UserDetails details = loadUserInfoByUniqueKey(username);
        AssertUtil.isTrue(!Objects.equals(details.getPassword(), password), () -> {
            return new SimpleAuthException("用户名或密码错误");
        });
        com.tbs.web.security.ITokenModel model =
            SimpleTokenModel.create(UuidUtil.getInstance().getUuidString(), authProperty.getTokenSource(),
            authProperty.getTokenFiledName(),
            details.getUsername(), null);
        HolderUtil.operateDataSource(getTokenModelContext(), context -> {
            if (!context.putIfAbsent(model.getToken(), details)) {
                AssertUtil.throwError(() -> new SimpleAuthException("Token已存在"));
            }
        });
        return model;

    }

    @Override
    public void authToken(com.tbs.web.security.ITokenModel iTokenModel) throws AuthenticationException {
        if (StrUtil.isNotEmpty(iTokenModel.getToken())) {
            if (loadUserInfoByToken(iTokenModel) != null) {
                return;
            }
        }
        AssertUtil.throwError(() -> new SimpleAuthException("Token错误"));
    }

    @Override
    public boolean logout(com.tbs.web.security.ITokenModel iTokenModel) {
        HolderUtil.operateDataSource(tokenModelContext, context -> {
            context.remove(iTokenModel.getToken());
        });
        return true;
    }
}
