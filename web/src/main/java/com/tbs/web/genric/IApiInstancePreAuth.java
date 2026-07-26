package com.tbs.web.genric;

import com.tbs.web.genric.form.HttpInvokeForm;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 鉴权前置处理
 *
 * @author abstergo
 */
public interface IApiInstancePreAuth {

    /**
     * 获取鉴权前置处理的id
     *
     * @return
     */
    String getPreAuthId();

    /**
     * 鉴权前置处理
     *
     * @param userDetails
     * @param request
     * @param apiInstance
     * @throws AuthenticationException
     */
    void preAuth(UserDetails userDetails, HttpInvokeForm request, AbstractGenricApiInstance apiInstance)
        throws AuthenticationException;
}
