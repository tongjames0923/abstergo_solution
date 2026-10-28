package com.tbs.common.provider.impl.coordinator;

import com.tbs.common.constant.ProviderConstant;
import com.tbs.common.provider.IRouteCoordinator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 全接受路由协调器
 * <p>
 * 不做事后裁决，语义与 {@link IRouteCoordinator.Default} 一致。区别只在装配方式：本类登记为容器 Bean， 供需要显式注入 {@link IRouteCoordinator}
 * 的调用点使用；注解上的缺省值则反射使用 {@link IRouteCoordinator.Default}。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(prefix = ProviderConstant.PROPERTY_PREFIX, name = ProviderConstant.ENABLE_PROPERTY_NAME,
    matchIfMissing = true)
public class AcceptAllRouteCoordinator extends IRouteCoordinator.Default {
}
