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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
     * 获取注册包的名称
     * <p>
     * 用于标识当前注册器扫描的包
     * </p>
     *
     * @return 注册包的名称
     */
    protected abstract String getPackName();

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
            var count = scanner.scan(getScanningPackages());
            end(count);
            return true;
        });

    }
}
