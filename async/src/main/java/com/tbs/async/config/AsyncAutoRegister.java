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
 * 异步包自动注册类
 * <p>
 * 继承自AbstractAutoBeanRegister，用于自动注册和扫描async包中的组件
 * 根据EnableTbsAsyncPack注解的配置决定是否扫描广播包
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class AsyncAutoRegister extends AbstractAutoBeanRegister {
    /**
     * 获取注册包名称
     * <p>
     * 返回异步包的名称标识
     * </p>
     *
     * @return 注册包名称
     */
    @Override
    protected String getPackName() {
        return "Async Package";
    }

    private String[] m_packages = new String[] {"com.tbs.async", "com.tbs.lock", "com.tbs.thread"};

    private Collection<String> basePackages = new HashSet<>();


    /**
     * 获取基础包路径数组
     * <p>
     * 返回需要扫描的基础包列表
     * </p>
     *
     * @return 基础包路径数组
     */
    @Override
    protected String[] getScanningPackages() {
        for (String packageName : m_packages) {
            basePackages.add(packageName);
        }
        String[] packages = new String[basePackages.size()];
        packages = basePackages.toArray(packages);
        return packages;
    }

    /**
     * 自动注册开始前的处理方法
     * <p>
     * 根据EnableTbsAsyncPack注解的配置决定是否添加广播包的扫描
     * </p>
     *
     * @param scanner                   类路径Bean定义扫描器
     * @param importingClassMetadata    导入类的注解元数据
     * @param registry                  Bean定义注册表
     * @param importBeanNameGenerator   导入Bean名称生成器
     */
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

        var useSchedule = enableTbsAsyncPack.getBoolean("useSchedule");
        if (useSchedule) {
            basePackages.add("com.tbs.schedule");
        }
    }
}
