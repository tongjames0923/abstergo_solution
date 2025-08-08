package com.tbs.web.security.impl;

import com.tbs.web.security.IUserInfoService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Resource;

/**
 * 默认用户认证服务实现
 **/
public class DefaultUserDetailsService implements UserDetailsService {

    @Lazy
    @Resource
    private IUserInfoService userInfoService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userInfoService.loadUserInfoByUniqueKey(username);
    }
}
