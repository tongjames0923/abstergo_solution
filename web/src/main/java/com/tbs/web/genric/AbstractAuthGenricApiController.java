package com.tbs.web.genric;

import com.tbs.web.uitl.AuthUtil;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 泛化接口控制器
 *
 * @author abstergo
 */
public abstract class AbstractAuthGenricApiController extends AbstractGenricApiController {
    @Override
    protected boolean support(AbstractGenricApiInstance instance) {
        return instance.isNeedAuth();
    }

    @Override
    protected UserDetails getCurrentUser() {
        return AuthUtil.getInstance().getCurrentUser();
    }
}
