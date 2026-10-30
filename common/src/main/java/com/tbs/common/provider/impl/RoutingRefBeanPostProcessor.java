package com.tbs.common.provider.impl;

import java.lang.reflect.Field;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringValueResolver;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.provider.RoutingStrategy;
import com.tbs.common.provider.annotation.RoutingRef;

import lombok.extern.slf4j.Slf4j;

/**
 * {@link RoutingRef} 字段装配器
 * <p>
 * 扫描业务 Bean 中标注 {@link RoutingRef} 的字段，按字段类型（目标接口）与声明的 business/version 坐标，
 * 经统一注册中心构建绑定该坐标的路由代理并注入字段，实现「区分版本与业务分区的分类装配」。
 * 与 {@code @RoutingService} 的主代理 Bean 正交：本装配器只写注入点字段，不注册任何 Bean 定义。
 * 坐标与策略字符串均经容器占位符解析器还原（支持 {@code ${...}}），留空表示该维度不约束。
 * 注册中心在首个 {@link RoutingRef} 字段注入时按需从容器取得，避免装配器提前拉起中心。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class RoutingRefBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware, EmbeddedValueResolverAware {

    /**
     * 字段类型必须是接口这一校验的资源键
     */
    private static final String KEY_NOT_INTERFACE = "provider.routing.ref.target.not.interface";

    /**
     * 绑定代理注入失败这一兜底异常的资源键
     */
    private static final String KEY_INJECT_FAILED = "provider.routing.ref.inject.failed";

    /**
     * 应用上下文，注入时按需解析统一注册中心
     */
    private ApplicationContext applicationContext;

    /**
     * 占位符解析器，还原 business/version/strategy 中的 {@code ${...}}，容器无嵌入值能力时可为null
     */
    private StringValueResolver embeddedValueResolver;

    /**
     * 注入应用上下文
     *
     * @param applicationContext 应用上下文，不能为null
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 注入占位符解析器
     *
     * @param resolver 嵌入值解析器，可为null
     */
    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.embeddedValueResolver = resolver;
    }

    /**
     * Bean 初始化前注入其 {@link RoutingRef} 字段
     *
     * @param bean     目标 Bean，不能为null
     * @param beanName Bean 名称，不能为null
     * @return 原样返回 Bean（仅改写字段，不代理）
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), field -> injectIfRoutingRef(bean, field),
            field -> field.isAnnotationPresent(RoutingRef.class));
        return bean;
    }

    /**
     * 装配单个 {@link RoutingRef} 字段
     * <p>
     * 先校验字段类型为接口（非接口属配置错误，快速失败）；再解析坐标与策略、向中心取绑定代理并反射注入。
     * 取中心/构建/注入过程中的任何异常统一包装为带字段定位的系统级异常，便于启动阶段定位配置问题。
     * </p>
     *
     * @param bean  宿主 Bean，不能为null
     * @param field 标注 {@link RoutingRef} 的字段，不能为null
     */
    private void injectIfRoutingRef(Object bean, Field field) {
        RoutingRef routingRef = field.getAnnotation(RoutingRef.class);
        if (routingRef == null) {
            return;
        }
        Class<?> interfaceType = field.getType();
        AssertUtil.isTrue(!interfaceType.isInterface()).i18n(KEY_NOT_INTERFACE)
            .params(bean.getClass().getName(), field.getName())
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        try {
            String business = resolveValue(routingRef.business());
            String version = resolveValue(routingRef.version());
            RoutingStrategy strategy = RoutingStrategy.byName(resolveValue(routingRef.strategy()));
            IRegistryCenter registryCenter = applicationContext.getBean(IRegistryCenter.class);
            Object proxy = registryCenter.getRoutingProxy(interfaceType, strategy, business, version);
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, bean, proxy);
            log.debug("[RoutingRef] 注入绑定代理, 字段: {}#{}, 接口: {}, business: {}, version: {}, 策略: {}",
                bean.getClass().getName(), field.getName(), interfaceType.getName(), business, version, strategy);
        } catch (Exception e) {
            // 不把外部异常原文拼进消息（该消息属对外文案），真实原因作为 cause 交给日志
            AssertUtil.isTrue(true).i18n(KEY_INJECT_FAILED)
                .params(bean.getClass().getName(), field.getName())
                .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).cause(e).throwIt();
        }
    }

    /**
     * 解析注解字符串值：空值归一为空串，非空经占位符解析器还原 {@code ${...}}
     *
     * @param raw 注解原始值，可为null
     * @return 解析后的值，空表示不约束
     */
    private String resolveValue(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        return embeddedValueResolver != null ? embeddedValueResolver.resolveStringValue(raw) : raw;
    }
}
