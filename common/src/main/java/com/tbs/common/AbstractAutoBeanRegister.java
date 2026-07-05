package com.tbs.common;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象自动Bean注册器
 * <p>
 * 用于自动注册Spring Bean定义的抽象基类。实现了ImportBeanDefinitionRegistrar接口， 提供了扫描指定包和注解的Bean并自动注册到Spring容器的功能。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractAutoBeanRegister implements ImportBeanDefinitionRegistrar {

    /**
     * 已注册的功能模块集合
     * <p>
     * 使用ConcurrentHashMap.newKeySet()确保线程安全，
     * 当某个功能对应的包被扫描注册后，该功能即被视为已启用。
     * </p>
     */
    private static final Set<Feature> REGISTERED_FEATURES = ConcurrentHashMap.newKeySet();

    /**
     * 获取注册包的名称
     * <p>
     * 用于标识当前注册器扫描的包
     * </p>
     *
     * @return 注册包的名称
     */
    protected abstract String getPackName();

    /**
     * 获取当前注册器对应的功能模块
     * <p>
     * 子类可以覆写此方法，声明该注册器所代表的功能模块。
     * 返回非null值时，该功能将在扫描完成后被标记为已注册。
     * 默认返回null，表示不关联任何功能模块。
     * </p>
     *
     * @return 对应的功能模块，返回null表示不关联功能
     */
    protected Feature getFeature() {
        return null;
    }

    /**
     * 获取所有已注册的功能模块（不可修改）
     *
     * @return 已注册功能模块的不可修改集合
     */
    public static Set<Feature> getRegisteredFeatures() {
        return Collections.unmodifiableSet(REGISTERED_FEATURES);
    }

    /**
     * 检查指定功能是否已注册
     *
     * @param feature 要检查的功能模块
     * @return true表示已注册，false表示未注册
     */
    public static boolean isFeatureRegistered(Feature feature) {
        return feature != null && REGISTERED_FEATURES.contains(feature);
    }

    private static final Map<String, Boolean> PACK_MAP = new HashMap<>(32);

    /**
     * 获取需要扫描的基础包路径
     * <p>
     * 返回一个字符串数组，包含所有需要扫描的包路径
     * </p>
     *
     * @return 需要扫描的基础包路径数组
     */
    protected abstract String[] getScanningPackages();

    /**
     * 获取需要扫描的注解类型
     * <p>
     * 返回一个注解类型集合，默认包含Component和Configuration注解
     * </p>
     *
     * @return 需要扫描的注解类型集合
     */
    protected Collection<Class<? extends Annotation>> getScanAnnotations() {
        return CollUtil.newArrayList(Component.class, Configuration.class);
    }

    /**
     * 扫描开始 可以在扫描前进行一些操作
     *
     * @param scanner
     */
    protected void begin(ClassPathBeanDefinitionScanner scanner, AnnotationMetadata importingClassMetadata,
        BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        log.info("开始扫描包: {}", getPackName());
    }

    /**
     * 扫描结束 扫描结束 可以进行一些操作
     */
    protected void end(int count) {
        log.info("扫描包: {} 完成, 共扫描到 {} 个 Bean", getPackName(), count);
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry,
        BeanNameGenerator importBeanNameGenerator) {
        PACK_MAP.computeIfAbsent(getPackName(), k -> {
            ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, true);
            begin(scanner, importingClassMetadata, registry, importBeanNameGenerator);
            var scanAnnotations = getScanAnnotations();
            if (CollUtil.isNotEmpty(scanAnnotations)) {
                scanAnnotations.stream().filter(annotation -> annotation != null)
                    .forEach(scanAnnotation -> scanner.addIncludeFilter(new AnnotationTypeFilter(scanAnnotation)));
            }
            var packages = getScanningPackages();
            if (com.tbs.common.util.base.CollUtil.isEmpty(packages)) {
                return true;
            }
            var count = scanner.scan(packages);
            end(count);

            // 注册功能模块
            Feature feature = getFeature();
            if (feature != null) {
                REGISTERED_FEATURES.add(feature);
                log.info("功能模块 [{}] 已注册", feature.getDescription());
            }

            return true;
        });

    }
}
