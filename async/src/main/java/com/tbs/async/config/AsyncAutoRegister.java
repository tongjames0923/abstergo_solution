package com.tbs.async.config;

import com.tbs.async.annotation.EnableTbsAsyncPack;
import com.tbs.common.AbstractAutoBeanRegister;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Collection;
import java.util.HashSet;

/**
 * 异步注册
 *
 * @author tongj
 */
public class AsyncAutoRegister extends AbstractAutoBeanRegister {
    @Override
    protected String getPackName() {
        return "Async Package";
    }

    private String[] m_packages = new String[] {"com.tbs.async", "com.tbs.lock", "com.tbs.schedule", "com.tbs.thread"};

    private Collection<String> basePackages = new HashSet<>();


    @Override
    protected String[] getBasePackages() {
        for (String packageName : m_packages) {
            basePackages.add(packageName);
        }
        String[] packages = new String[basePackages.size()];
        packages = basePackages.toArray(packages);
        return packages;
    }

    @Override
    protected void begin(ClassPathBeanDefinitionScanner scanner, AnnotationMetadata importingClassMetadata,
        BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        var annotations = importingClassMetadata.getAnnotations();
        var enableTbsAsyncPack = annotations.get(EnableTbsAsyncPack.class);
        if (enableTbsAsyncPack == null) {
            return;
        }
        var useBroadcast = enableTbsAsyncPack.getBoolean("useBroadcast");
        if (useBroadcast) {
            basePackages.add("com.tbs.broadcast");
        }
    }
}
