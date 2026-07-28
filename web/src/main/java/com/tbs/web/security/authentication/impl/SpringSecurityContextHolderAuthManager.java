package com.tbs.web.security.authentication.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.web.security.authentication.AbstractAuthManager;
import com.tbs.web.security.authentication.model.ITokenAuthentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SpringSecurityContextHolderAuthManager
 *
 * @author abstergo
 */
public class SpringSecurityContextHolderAuthManager extends AbstractAuthManager {
    @Override
    public void setAuthentication(ITokenAuthentication authentication) {
        AssertUtil.isTrue(authentication == null, () -> new NullPointerException("authentication cannot be null"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    public void clearAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Override
    public void clearAuthentication(String token) {
        AssertUtil.throwError(() -> new UnsupportedOperationException(
            "SpringSecurityContextHolderAuthManager does not support clearAuthentication(String token)"));
    }

    @Override
    public ITokenAuthentication getCurrentAuthentication() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        if (!(authentication instanceof ITokenAuthentication)) {
            return null;
        }
        return (ITokenAuthentication)SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public ITokenAuthentication getAuthentication(String token) {
        AssertUtil.throwError(() -> new UnsupportedOperationException(
            "SpringSecurityContextHolderAuthManager does not support getAuthentication(String token)"));
        return null;
    }
}
