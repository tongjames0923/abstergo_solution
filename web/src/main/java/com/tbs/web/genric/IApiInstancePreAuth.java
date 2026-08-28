package com.tbs.web.genric;

import com.tbs.web.genric.form.HttpInvokeForm;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 鉴权前置处理器
 * <p>
 * 在泛化实例真正执行远程调用前做额外的鉴权/上下文装配（如数据权限、租户校验等）。
 * 通过 {@link #getPreAuthId} 注册到实例的预认证链，由实例在调用前按序触发。
 * </p>
 *
 * @author abstergo
 */
public interface IApiInstancePreAuth {

    /**
     * 获取鉴权前置处理的唯一 id
     *
     * @return 预认证处理器标识
     */
    String getPreAuthId();

    /**
     * 鉴权前置处理
     *
     * @param userDetails 当前用户详情，可为 {@code null}（匿名场景）
     * @param request     原始调用表单，可为 {@code null}
     * @param apiInstance 待执行的泛化实例，不能为空
     * @throws AuthenticationException 当鉴权不通过时抛出
     */
    void preAuth(UserDetails userDetails, HttpInvokeForm request, AbstractGenricApiInstance apiInstance)
        throws AuthenticationException;
}
