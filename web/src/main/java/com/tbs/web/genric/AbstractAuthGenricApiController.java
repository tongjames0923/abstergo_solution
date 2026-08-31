package com.tbs.web.genric;

import com.tbs.web.uitl.AuthUtil;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 需认证的泛化接口控制器
 * <p>
 * 仅承接 {@code needAuth=true} 的实例，当前用户由 {@link AuthUtil} 从认证上下文获取。
 * </p>
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
