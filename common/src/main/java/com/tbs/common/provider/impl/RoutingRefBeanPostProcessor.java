package com.tbs.common.provider.impl;

import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.constant.ProviderConstant;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.provider.INoProviderHandler;
import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.provider.IRouteCoordinator;
import com.tbs.common.provider.annotation.RoutingRef;
import com.tbs.common.provider.vo.InstanceProxy;
import com.tbs.common.util.base.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link RoutingRef} 字段装配器
 * <p>
 * 在 Bean 初始化前扫描字段，为每个 {@link RoutingRef} 字段写入一个绑定坐标的 JDK 动态代理：字段类型即目标接口， 注解上的 business/version 经容器 Environment
 * 解析占位符后作为寻址坐标。
 * </p>
 * <p>
 * 代理不缓存实例，每次调用都经注册中心按坐标取当前可用的实例，来源上下线在下一个调用即生效； 未命中时交由 {@link RoutingRef#noProvider()} 处理并返回 {@code null}（基本类型返回默认值）。
 * 注册中心以 {@link ObjectProvider} 延迟到调用期获取，避免与贡献者链构成初始化环。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = ProviderConstant.PROPERTY_PREFIX, name = ProviderConstant.ENABLE_PROPERTY_NAME,
    matchIfMissing = true)
public class RoutingRefBeanPostProcessor implements BeanPostProcessor {

    /**
     * 注册中心提供器，调用期才取实例
     */
    private final ObjectProvider<IRegistryCenter> registryCenterProvider;

    /**
     * 容器 Bean 工厂，用于复用容器内已存在的无提供者处理器
     */
    private final ListableBeanFactory beanFactory;

    /**
     * 容器环境，用于解析坐标上的占位符
     */
    private final Environment environment;

    /**
     * 类到绑定字段的缓存，避免每个 Bean 重复反射扫描
     */
    private final Map<Class<?>, List<Field>> fieldCache = new ConcurrentHashMap<>();

    /**
     * 无提供者处理器缓存，同一处理类只装配一次
     */
    private final Map<Class<? extends INoProviderHandler>, INoProviderHandler> noProviderCache =
        new ConcurrentHashMap<>();

    /**
     * 路由协调器缓存，同一协调器类型只装配一次
     */
    private final Map<Class<? extends IRouteCoordinator>, IRouteCoordinator> coordinatorCache =
        new ConcurrentHashMap<>();

    /**
     * 构造字段装配器
     *
     * @param registryCenterProvider 注册中心提供器，由容器注入
     * @param beanFactory            容器 Bean 工厂，由容器注入
     * @param environment            容器环境，由容器注入
     */
    public RoutingRefBeanPostProcessor(ObjectProvider<IRegistryCenter> registryCenterProvider,
        ListableBeanFactory beanFactory, Environment environment) {
        this.registryCenterProvider = registryCenterProvider;
        this.beanFactory = beanFactory;
        this.environment = environment;
    }

    /**
     * 初始化前为绑定字段写入代理
     *
     * @param bean     待初始化 Bean
     * @param beanName Bean 名称
     * @return 原 Bean
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        List<Field> fields = fieldsOf(bean.getClass());
        if (fields.isEmpty()) {
            return bean;
        }
        for (Field field : fields) {
            Object proxy = proxyOf(field);
            try {
                ReflectionUtils.makeAccessible(field);
                field.set(bean, proxy);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                AssertUtil.isTrueSystemError(true)
                    .i18n(CommonI18nConstant.ROUTING_REF_INJECT_FAILED, field.getDeclaringClass().getName(),
                        field.getName(), e.getMessage())
                    .throwIt();
            }
        }
        return bean;
    }

    /**
     * 取类上的绑定字段，结果按类缓存
     *
     * @param beanClass Bean 类型，不能为null
     * @return 绑定字段列表，无绑定为空列表
     */
    private List<Field> fieldsOf(Class<?> beanClass) {
        return fieldCache.computeIfAbsent(beanClass, type -> {
            List<Field> fields = new ArrayList<>();
            ReflectionUtils.doWithFields(type, field -> {
                if (field.isAnnotationPresent(RoutingRef.class)) {
                    fields.add(field);
                }
            });
            return List.copyOf(fields);
        });
    }

    /**
     * 为字段构建绑定坐标的代理
     *
     * @param field 绑定字段，不能为null
     * @return 动态代理
     */
    private Object proxyOf(Field field) {
        Class<?> targetType = field.getType();
        AssertUtil.isTrueSystemError(!targetType.isInterface())
            .i18n(CommonI18nConstant.ROUTING_REF_TARGET_NOT_INTERFACE, field.getDeclaringClass().getName(),
                field.getName()).throwIt();
        RoutingRef routingRef = field.getAnnotation(RoutingRef.class);
        String business = coordinateOf(routingRef.business());
        String version = coordinateOf(routingRef.version());
        RoutingRefInvocationHandler handler =
            new RoutingRefInvocationHandler(targetType, business, version, coordinatorOf(routingRef.coordinator()),
                registryCenterProvider, noProviderOf(routingRef.noProvider()));
        log.debug("[RoutingRef] 字段[{}.{}]已装配绑定代理，坐标: business=[{}], version=[{}], 协调器=[{}]",
            field.getDeclaringClass().getName(), field.getName(), business, version,
            routingRef.coordinator().getName());
        return Proxy.newProxyInstance(targetType.getClassLoader(), new Class<?>[] {targetType}, handler);
    }

    /**
     * 解析坐标值：空白视为不约束，其余按占位符展开
     *
     * @param coordinate 注解上的原始坐标值，可为null
     * @return 解析后的坐标值，空白为空串
     */
    private String coordinateOf(String coordinate) {
        if (StrUtil.isBlank(coordinate)) {
            return "";
        }
        return environment.resolvePlaceholders(coordinate).trim();
    }

    /**
     * 取无提供者处理器：优先用容器内的 Bean，缺失时反射构造
     *
     * @param noProviderClass 处理类，不能为null
     * @return 处理器实例
     */
    private INoProviderHandler noProviderOf(Class<? extends INoProviderHandler> noProviderClass) {
        return noProviderCache.computeIfAbsent(noProviderClass, type -> {
            INoProviderHandler handler = beanFactory.getBeanProvider(type).getIfAvailable();
            return handler == null ? instantiate(type) : handler;
        });
    }

    /**
     * 取路由协调器：优先用容器内的 Bean，缺失时反射构造
     *
     * @param coordinatorClass 协调器类型，不能为null
     * @return 协调器实例
     */
    private IRouteCoordinator coordinatorOf(Class<? extends IRouteCoordinator> coordinatorClass) {
        return coordinatorCache.computeIfAbsent(coordinatorClass, type -> {
            IRouteCoordinator coordinator = beanFactory.getBeanProvider(type).getIfAvailable();
            return coordinator == null ? instantiate(type) : coordinator;
        });
    }

    /**
     * 反射构造无参对象
     *
     * @param type 目标类型，不能为null
     * @param <T>  目标类型
     * @return 新实例
     */
    private <T> T instantiate(Class<T> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw AssertUtil.isTrueSystemError(true)
                .i18n(CommonI18nConstant.ROUTING_REF_INSTANTIATE_FAILED, type.getName(), e.getMessage()).build();
        }
    }

    /**
     * 基本类型的默认值，非基本类型返回 null
     *
     * @param returnType 方法返回类型，不能为null
     * @return 默认值
     */
    private static Object defaultValueOf(Class<?> returnType) {
        if (!returnType.isPrimitive() || returnType == void.class) {
            return null;
        }
        return Array.get(Array.newInstance(returnType, 1), 0);
    }

    /**
     * 坐标绑定代理的调用处理器
     * <p>
     * 每次调用按坐标向注册中心取实例，未命中交由无提供者处理器处理；{@link Object} 方法与来源无关，本地应答。
     * </p>
     */
    private static final class RoutingRefInvocationHandler implements InvocationHandler {

        /**
         * 目标接口
         */
        private final Class<?> targetType;

        /**
         * 业务分区，空串表示不约束
         */
        private final String business;

        /**
         * 发布版本，空串表示不约束
         */
        private final String version;

        /**
         * 候选裁决协调器
         */
        private final IRouteCoordinator coordinator;

        /**
         * 注册中心提供器
         */
        private final ObjectProvider<IRegistryCenter> registryCenterProvider;

        /**
         * 无提供者处理器
         */
        private final INoProviderHandler noProviderHandler;

        private RoutingRefInvocationHandler(Class<?> targetType, String business, String version,
            IRouteCoordinator coordinator, ObjectProvider<IRegistryCenter> registryCenterProvider,
            INoProviderHandler noProviderHandler) {
            this.targetType = targetType;
            this.business = business;
            this.version = version;
            this.coordinator = coordinator;
            this.registryCenterProvider = registryCenterProvider;
            this.noProviderHandler = noProviderHandler;
        }

        /**
         * 按坐标解析并转发调用
         *
         * @param proxy  代理对象
         * @param method 被调方法
         * @param args   调用参数
         * @return 调用结果，无提供者时为默认值
         * @throws Throwable 目标实例抛出的异常
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return invokeObjectMethod(proxy, method, args);
            }
            InstanceProxy instanceProxy =
                registryCenterProvider.getObject().getInstanceProxy(targetType, business, version, coordinator);
            if (instanceProxy == null || instanceProxy.getInstance() == null) {
                noProviderHandler.handle(business, version, targetType);
                return defaultValueOf(method.getReturnType());
            }
            try {
                return method.invoke(instanceProxy.getInstance(), args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

        /**
         * 应答 {@link Object} 的方法，避免触发来源解析
         *
         * @param proxy  代理对象
         * @param method 被调方法
         * @param args   调用参数
         * @return 方法结果
         */
        private Object invokeObjectMethod(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "toString" ->
                    "RoutingRef[" + targetType.getName() + ", business=" + business + ", version=" + version + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> null;
            };
        }
    }
}
