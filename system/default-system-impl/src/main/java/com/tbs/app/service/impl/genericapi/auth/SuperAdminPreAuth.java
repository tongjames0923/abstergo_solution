package com.tbs.app.service.impl.genericapi.auth;

import com.tbs.common.error.AssertUtil;
import com.tbs.shared.constant.RoleConstants;
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
        AssertUtil.isTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> RoleConstants.SUPER_ADMIN_ROLE_NAME.equals(authority.getAuthority())))
            .as(e -> new AuthenticationException(e.getMessage()) {
            }).throwIt();
    }
}
