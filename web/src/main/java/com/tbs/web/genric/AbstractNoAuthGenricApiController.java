package com.tbs.web.genric;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * 匿名（无需认证）泛化接口控制器
 * <p>
 * 仅承接 {@code needAuth=false} 的实例，当前用户恒为 {@code null}（实例内部不再做登录校验）。
 * </p>
 *
 * @author abstergo
 */
public abstract class AbstractNoAuthGenricApiController extends AbstractGenricApiController {
    @Override
    protected boolean support(AbstractGenricApiInstance instance) {
        return !instance.isNeedAuth();
    }

    @Override
    protected UserDetails getCurrentUser() {
        return null;
    }
}
