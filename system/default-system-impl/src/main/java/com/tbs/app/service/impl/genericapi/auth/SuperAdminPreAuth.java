package com.tbs.app.service.impl.genericapi.auth;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.genric.AbstractGenricApiInstance;
import com.tbs.web.genric.IApiInstancePreAuth;
import com.tbs.web.genric.form.HttpInvokeForm;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SuperAdminPreAuth implements IApiInstancePreAuth {

    @Override
    public String getPreAuthId() {
        return "SuperAdminUse";
    }

    @Override
    public void preAuth(UserDetails userDetails, HttpInvokeForm request, AbstractGenricApiInstance apiInstance)
        throws AuthenticationException {
        AssertUtil.isTrue(!userDetails.getAuthorities().stream()
                .anyMatch(authority -> "管理员".equals(authority.getAuthority())))
            .i18n("generic.api.auth.superadmin.pre.auth.fail")
            .params(apiInstance.getApiId())
            .as(e -> new AuthenticationException(e.getMessage()) {
            }).throwIt();
    }
}
