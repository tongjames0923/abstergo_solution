package com.tbs.app.service.impl.genericapi.auth;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.genric.AbstractGenricApiInstance;
import com.tbs.web.genric.IApiInstancePreAuth;
import com.tbs.web.genric.form.HttpInvokeForm;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.tbs.app.constant.SystemI18nConstant;

/**
 * 账号激活预授权
 *
 * @author abstergo
 */
@Component
public class AccountActivePreAuth implements IApiInstancePreAuth {
    @Override
    public String getPreAuthId() {
        return "AccountActived";
    }

    @Override
    public void preAuth(UserDetails userDetails, HttpInvokeForm request, AbstractGenricApiInstance apiInstance)
        throws AuthenticationException {
        AssertUtil.isTrue(userDetails.isEnabled()&&!userDetails.isAccountNonLocked()).i18n(com.tbs.app.constant.SystemI18nConstant.AUTH_USER_NOT_ACTIVATED)
            .as(e -> new AuthenticationException(e.getMessage()) {
            }).throwIt();
    }
}
