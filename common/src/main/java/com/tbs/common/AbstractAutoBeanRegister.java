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
 * 抽象自动注册 用于自动注册Bean定义
 *
 * @author tongj
 */
@Slf4j
public abstract class AbstractAutoBeanRegister implements ImportBeanDefinitionRegistrar {

    /**
     * 获取包名
     *
     * @return
     */
    protected abstract String getPackName();

    private static final Map<String, Boolean> PACK_MAP = new HashMap<>(32);

    /**
     * 获取扫描的包
     *
     * @return
     */
    protected abstract String[] getBasePackages();

    /**
     * 获取扫描的注解
     *
     * @return
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
            var count = scanner.scan(getBasePackages());
            end(count);
            return true;
        });

    }
}
