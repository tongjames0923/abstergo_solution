package com.tbs.web.genric;

import com.tbs.web.uitl.AuthUtil;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 需认证的泛化接口控制器
 * <p>
 * 仅承接 {@code needAuth=true} 的实例，当前用户由 {@link AuthUtil} 从认证上下文获取。
 * </p>
 * <p>
 * 本类属于旧泛化调用链路；新链路的认证入口沿用同一套 {@link AuthUtil} 与预认证 Bean，但不再经过本类层级。
 * </p>
 *
 * @author abstergo
 * @deprecated 旧认证维度的信封式入口基类。替代：{@link AbstractGenricInvokeController} 及其具体子类；
 * 旧入口无调用量后的首个发布版本移除。
 */
@Deprecated
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
