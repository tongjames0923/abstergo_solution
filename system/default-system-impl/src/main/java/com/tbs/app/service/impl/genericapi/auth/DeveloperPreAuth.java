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
 * @author abstergo
 */
@Component
public class DeveloperPreAuth implements IApiInstancePreAuth {
    @Override
    public String getPreAuthId() {
        return "DeveloperUse";
    }

    @Override
    public void preAuth(UserDetails userDetails, HttpInvokeForm request, AbstractGenricApiInstance apiInstance)
        throws AuthenticationException {
        AssertUtil.isTrue(!userDetails.getAuthorities().stream()
                .anyMatch(authority -> "开发者".equals(authority.getAuthority())))
            .i18n(com.tbs.app.constant.SystemI18nConstant.GENERIC_API_AUTH_DEVELOPER_PRE_AUTH_FAIL).params(apiInstance.getApiId())
            .as(e -> new AuthenticationException(e.getMessage()) {
            }).throwIt();
    }
}
