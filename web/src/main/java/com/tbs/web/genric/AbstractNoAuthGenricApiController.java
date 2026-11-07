package com.tbs.web.genric;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * 匿名（无需认证）泛化接口控制器
 * <p>
 * 仅承接 {@code needAuth=false} 的实例，当前用户恒为 {@code null}（实例内部不再做登录校验）。
 * </p>
 * <p>
 * 本类属于旧泛化调用链路；新链路用独立的匿名入口控制器，不再经过本类层级。
 * </p>
 *
 * @author abstergo
 * @deprecated 旧匿名维度的信封式入口基类。替代：{@link AbstractGenricInvokeController} 及其匿名子类；
 * 旧入口无调用量后的首个发布版本移除。
 */
@Deprecated
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
