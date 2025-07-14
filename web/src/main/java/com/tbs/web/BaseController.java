package com.tbs.web;

import com.tbs.web.security.model.TokenModel;
import com.tbs.web.trace.core.TraceObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;

/**
 * 基础的控制器, 提供获取当前用户和TokenModel的方法
 *
 * @author tongj
 */
public class BaseController {

    @Resource
    private TraceObject traceObject;

    /**
     * 获取当前用户的TokenModel
     *
     * @return
     */
    public TokenModel getCurrentTokenModel() {
        return (TokenModel)getAuthentication().getCredentials();
    }

    /**
     * 获取当前用户
     *
     * @return
     */
    public UserDetails getCurrentUser() {
        return (UserDetails)getAuthentication().getPrincipal();
    }

    /**
     * 判断当前用户是否已认证（已登录）
     *
     * @return
     */
    public boolean isUserLoggedIn() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return false;
        }
        return SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
    }

    /**
     * 获取当前用户的认证信息
     *
     * @return
     */
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前用户的TraceObject
     *
     * @return
     */
    public TraceObject getTraceObject() {
        return traceObject;
    }
}
