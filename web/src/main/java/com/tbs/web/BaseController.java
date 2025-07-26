package com.tbs.web;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.exception.NoLoginException;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.ITokenAuthentication;
import com.tbs.web.security.ITokenModel;
import com.tbs.web.security.impl.TokenAuthentication;
import com.tbs.web.security.model.SimpleTokenModel;
import com.tbs.web.trace.core.TraceObject;
import com.tbs.web.uitl.AjaxUtil;
import lombok.Getter;
import lombok.val;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;

/**
 * 基础的控制器, 提供获取当前用户和TokenModel的方法
 *
 * @author tongj
 */
@Getter
public class BaseController {

    /**
     * -- GETTER -- 获取当前用户的TraceObject
     *
     * @return
     */
    @Lazy
    @Resource
    private TraceObject traceObject;

    /**
     * 获取当前用户的TokenModel
     *
     * @return
     */
    public ITokenModel getCurrentTokenModel() {
        AssertUtil.isTrue(!isUserLoggedIn(), () -> new NoLoginException("当前用户未登录"));
        return (ITokenModel)getAuthentication().getCredentials();
    }

    /**
     * 获取当前用户
     *
     * @return
     */
    public UserDetails getCurrentUser() {
        return getAuthentication().getUserDetails();
    }

    /**
     * 判断当前用户是否已认证（已登录）
     *
     * @return
     */
    public boolean isUserLoggedIn() {
        ITokenAuthentication authentication = getAuthentication();
        return authentication != null && authentication.hasToken() && authentication.isAuthenticated();
    }

    /**
     * 获取当前用户的认证信息
     *
     * @return
     */
    public ITokenAuthentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        if (!(authentication instanceof TokenAuthentication)) {
            return null;
        }
        return (ITokenAuthentication)authentication;
    }

    /**
     * 默认执行的操作成功返回值
     *
     * @param val
     * @return
     */
    public AjaxResult successResult(Object val) {
        return AjaxUtil.getInstance().success("操作成功", val);
    }

    /**
     * 默认执行的操作失败返回值
     *
     * @param e
     * @return
     */
    public AjaxResult errorResult(Throwable e) {
        return AjaxUtil.getInstance().error("系统异常。" + e.getMessage(), e);
    }

}
