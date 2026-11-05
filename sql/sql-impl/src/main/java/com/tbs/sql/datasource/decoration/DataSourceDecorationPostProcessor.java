package com.tbs.sql.datasource.decoration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DataSource 装饰后置处理器：在 Bean 初始化后将容器中的目标 {@link DataSource}
 * 包装为 {@link TbsDataSourceDecorator}，使其获得 JDBC 执行拦截能力。
 *
 * <h3>目标选取策略（运行时判定）</h3>
 * <ul>
 *   <li>容器内唯一 DataSource Bean：装饰该 Bean</li>
 *   <li>存在多个候选：仅装饰标注 {@code @Primary} 的 Bean</li>
 *   <li>多个候选且无 Primary：跳过装饰并告警（不猜测、不误伤次要数据源）</li>
 * </ul>
 *
 * <h3>防御策略</h3>
 * <ul>
 *   <li>非 DataSource Bean 与已装饰的 Bean 直接跳过，保证装饰幂等</li>
 *   <li>拦截器列表经 {@link InterceptorChain} 内置的 Supplier 惰性解析，
 *       避开装配期用户 Bean 未实例化导致的空链问题</li>
 *   <li>本处理器由 {@code DataSourceDecorationConfiguration} 以 static {@code @Bean}
 *       注册，确保早于任何单例实例化完成注册，装饰不会漏掉目标 Bean</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class DataSourceDecorationPostProcessor implements BeanPostProcessor, BeanFactoryAware {

    /**
     * 拦截器链
     */
    private final InterceptorChain chain;

    /**
     * Bean 工厂，用于运行时解析 DataSource 候选
     */
    private ConfigurableListableBeanFactory beanFactory;

    /**
     * 多候选无 Primary 的告警去重标记
     */
    private final AtomicBoolean ambiguousWarned = new AtomicBoolean(false);

    /**
     * 构造装饰后置处理器。
     *
     * @param chain 拦截器链
     */
    public DataSourceDecorationPostProcessor(InterceptorChain chain) {
        this.chain = chain;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!(bean instanceof DataSource) || bean instanceof TbsDataSourceDecorator) {
            return bean;
        }
        if (!shouldDecorate(beanName)) {
            return bean;
        }
        return new TbsDataSourceDecorator((DataSource) bean, chain, beanName);
    }

    @Override
    public void setBeanFactory(org.springframework.beans.factory.BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    /**
     * 判定给定 Bean 是否为应被装饰的数据源目标。
     *
     * @param beanName 候选 Bean 名称
     * @return true 表示应装饰
     */
    private boolean shouldDecorate(String beanName) {
        if (beanFactory == null) {
            return false;
        }
        String[] candidates = beanFactory.getBeanNamesForType(DataSource.class, true, false);
        if (candidates.length == 0) {
            return false;
        }
        if (candidates.length == 1) {
            return candidates[0].equals(beanName);
        }
        for (String candidate : candidates) {
            BeanDefinition definition = beanFactory.getBeanDefinition(candidate);
            if (definition.isPrimary() && candidate.equals(beanName)) {
                return true;
            }
        }
        if (ambiguousWarned.compareAndSet(false, true)) {
            log.warn("容器内存在多个 DataSource Bean 且未标注 @Primary，跳过 DataSource 装饰与 JDBC 执行拦截，candidates={}",
                    Arrays.toString(candidates));
        }
        return false;
    }
}
