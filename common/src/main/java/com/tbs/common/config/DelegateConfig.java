package com.tbs.common.config;

import com.tbs.common.config.property.DelegateProperty;
import com.tbs.common.delegate.IDelegateTemplate;
import com.tbs.common.delegate.impl.SimpleDelegateInvoker;
import com.tbs.common.delegate.impl.SpringManagedDelegateTemplate;
import com.tbs.common.delegate.provider.IDelegateFlagProvider;
import com.tbs.common.delegate.provider.IDelegateInvokerProvider;
import com.tbs.common.delegate.provider.IDelegateTargetProvider;
import com.tbs.common.delegate.provider.impl.CompletableFutureFlagProvider;
import com.tbs.common.delegate.provider.impl.SimpleCallerDelegateTargetProvider;
import com.tbs.common.delegate.provider.impl.SpringManagedTypeBaseInvokerProvider;
import com.tbs.common.util.base.BeanUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.Resource;

/**
 * @author tongj
 */
@Configuration
public class DelegateConfig {

    @Resource
    private DelegateProperty delegateProperty;

    @Bean
    @ConditionalOnMissingBean(IDelegateTemplate.class)
    IDelegateTemplate delegateTemplate() {
        return new SpringManagedDelegateTemplate();
    }

    @Bean
    @ConditionalOnMissingBean(IDelegateInvokerProvider.class)
    IDelegateInvokerProvider invokerProvider() {
        return new SpringManagedTypeBaseInvokerProvider();
    }

    @Bean
    @ConditionalOnMissingBean(IDelegateFlagProvider.class)
    IDelegateFlagProvider flagProvider() {
        return BeanUtil.buildBeanFromProperties(CompletableFutureFlagProvider::new,
            delegateProperty.getDelegateFlagProvider(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(IDelegateTargetProvider.class)
    IDelegateTargetProvider delegateTargetProvider() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new SimpleCallerDelegateTargetProvider();
        }, delegateProperty.getDelegateTargetProvider(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @Scope("prototype")
    SimpleDelegateInvoker simpleDelegateInvoker() {
        return new SimpleDelegateInvoker();
    }

}
