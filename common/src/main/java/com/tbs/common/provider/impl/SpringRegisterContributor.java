package com.tbs.common.provider.impl;

import com.tbs.common.constant.ProviderConstant;
import com.tbs.common.provider.AbstractRegisterContributor;
import com.tbs.common.provider.annotation.RoutingService;
import com.tbs.common.provider.vo.InstanceProxy;
import com.tbs.common.util.base.CollUtil;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring 容器注册贡献者
 * <p>
 * 以本地 Spring 容器为实例来源：按查询类型枚举容器内 Bean，仅登记被 {@link RoutingService} 声明的实现 （标注在实现类、{@code @Bean} 方法或所实现接口上均视为声明，未声明等价于未暴露），
 * 注解上的 business/version 即该实例的注册坐标，留空表示该维度不声明。
 * </p>
 * <p>
 * 优先级取 {@link ProviderConstant#ORDER_SPRING_RESOLVER}，在贡献者链中先于远端来源被遍历， 使本地实例命中即返回、不触发远程发现。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(prefix = ProviderConstant.PROPERTY_PREFIX, name = ProviderConstant.ENABLE_PROPERTY_NAME,
    matchIfMissing = true)
public class SpringRegisterContributor extends AbstractRegisterContributor implements Ordered {

    /**
     * 贡献者标识：本地 Spring 来源
     */
    private static final String CONTRIBUTOR_ID = "spring";

    /**
     * 容器 Bean 工厂，用于按类型枚举实例并读取声明注解
     */
    private final ListableBeanFactory beanFactory;

    /**
     * 构造 Spring 容器注册贡献者
     *
     * @param beanFactory 容器 Bean 工厂，由容器注入
     */
    public SpringRegisterContributor(ListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 获取注册贡献者ID
     *
     * @return 贡献者标识
     */
    @Override
    public String getContributorId() {
        return CONTRIBUTOR_ID;
    }

    /**
     * 获取贡献者优先级
     *
     * @return 本地来源优先级，数值小于远端来源
     */
    @Override
    public int getOrder() {
        return ProviderConstant.ORDER_SPRING_RESOLVER;
    }

    /**
     * 按类型枚举容器内的可路由实例
     * <p>
     * 只实例化被声明的 Bean，未声明的仅做名称枚举不触发初始化； 声明了 {@link RoutingService#interfaceClass()} 时以其为注册身份，仅在查询类型兼容该身份时登记。
     * </p>
     *
     * @param type 类型，不能为空
     * @param <T>  类型
     * @return 携带注册坐标的实例列表，无匹配时返回空列表
     */
    @Override
    protected <T> List<InstanceProxy> getInstanceByType(Class<T> type) {
        String[] beanNames = beanFactory.getBeanNamesForType(type, true, false);
        if (CollUtil.isEmpty(beanNames)) {
            return CollUtil.emptyList();
        }
        List<InstanceProxy> instanceProxies = new ArrayList<>(beanNames.length);
        for (String beanName : beanNames) {
            RoutingService routingService = beanFactory.findAnnotationOnBean(beanName, RoutingService.class);
            if (routingService == null) {
                continue;
            }
            Class<?> identity = routingService.interfaceClass();
            if (identity != void.class && !type.isAssignableFrom(identity)) {
                continue;
            }
            InstanceProxy proxy = new InstanceProxy();
            proxy.setInstance(type.cast(beanFactory.getBean(beanName)));
            proxy.setBusiness(routingService.business());
            proxy.setVersion(routingService.version());
            proxy.setContributor(this);
            instanceProxies.add(proxy);
        }
        return instanceProxies;
    }
}
