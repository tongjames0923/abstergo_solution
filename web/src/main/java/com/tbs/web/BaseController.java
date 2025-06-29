package com.tbs.web;

import com.tbs.web.security.model.TokenModel;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 基础的控制器
 *
 * @author tongj
 */
public class BaseController {
    /**
     * 获取当前用户的TokenModel
     *
     * @return
     */
    public TokenModel getCurrentTokenModel() {
        return (TokenModel)SecurityContextHolder.getContext().getAuthentication().getCredentials();
    }

    /**
     * 获取当前用户
     *
     * @return
     */
    public UserDetails getCurrentUser() {
        return (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
