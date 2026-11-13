package com.tbs.sql.metadata;

import com.tbs.sql.annotation.core.SqlEntityScan;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@link SqlEntityScan} 的装配器：解析注解上的包配置，注册唯一的
 * {@link EntityTableMetadataInitializer} 基础设施 Bean。
 *
 * <p>扫描动作不在本类中执行：此处仅收集包名，真正的类路径扫描与校验推迟到
 * {@link org.springframework.beans.factory.SmartInitializingSingleton} 阶段，
 * 以便用 {@code Environment} 解析 {@code ${...}} 占位符。</p>
 *
 * @author abstergo
 * @since 3.0.0
 * @see SqlEntityScan
 * @see EntityTableMetadataInitializer
 */
public class EntityScanRegistrar implements ImportBeanDefinitionRegistrar {

    static final String INITIALIZER_BEAN_NAME = EntityTableMetadataInitializer.class.getName();
    private static final String PACKAGES_ATTRIBUTE = EntityScanRegistrar.class.getName() + ".basePackages";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
            importingClassMetadata.getAnnotationAttributes(SqlEntityScan.class.getName()));
        if (attributes == null) {
            return;
        }

        Set<String> packages = new LinkedHashSet<>();
        collect(packages, attributes.getStringArray("value"));
        collect(packages, attributes.getStringArray("basePackages"));
        for (Class<?> markerClass : attributes.getClassArray("basePackageClasses")) {
            packages.add(ClassUtils.getPackageName(markerClass));
        }
        if (packages.isEmpty()) {
            packages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }

        if (registry.containsBeanDefinition(INITIALIZER_BEAN_NAME)) {
            merge(registry.getBeanDefinition(INITIALIZER_BEAN_NAME), packages);
            return;
        }
        RootBeanDefinition definition = new RootBeanDefinition(EntityTableMetadataInitializer.class);
        definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        definition.setAttribute(PACKAGES_ATTRIBUTE, packages);
        definition.getConstructorArgumentValues().addIndexedArgumentValue(0, packages);
        registry.registerBeanDefinition(INITIALIZER_BEAN_NAME, definition);
    }

    /**
     * 多个配置类分别标注 {@link SqlEntityScan} 时合并扫描包，保证只预热一次。
     *
     * @param definition 已注册的基础设施 Bean 定义
     * @param packages   本次解析出的包名
     */
    @SuppressWarnings("unchecked")
    private static void merge(BeanDefinition definition, Set<String> packages) {
        Set<String> merged = (Set<String>) definition.getAttribute(PACKAGES_ATTRIBUTE);
        if (merged == null) {
            merged = new LinkedHashSet<>();
            definition.setAttribute(PACKAGES_ATTRIBUTE, merged);
        }
        merged.addAll(packages);
        definition.getConstructorArgumentValues().addIndexedArgumentValue(0, merged);
    }

    /**
     * 收集非空的包名，占位符与逗号分隔留到初始化器阶段按 {@code Environment} 解析后再拆。
     *
     * @param target 包名集合
     * @param values 注解上声明的原始包名
     */
    private static void collect(Set<String> target, String[] values) {
        if (values == null) {
            return;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                target.add(value.trim());
            }
        }
    }
}
