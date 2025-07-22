package com.tbs.common.config;

import com.tbs.common.config.property.DelegateProperty;
import com.tbs.common.delegate.IDelegateProvider;
import com.tbs.common.delegate.impl.MainThreadCallDelegateProvider;
import com.tbs.common.util.BeanUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author abstergo
 */
@Configuration
public class DelegateConfig {
    @Resource
    private DelegateProperty delegateProperty;

    @Bean
    @ConditionalOnMissingBean(IDelegateProvider.class)
    public IDelegateProvider delegateProvider() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new MainThreadCallDelegateProvider();
        }, delegateProperty.getDelegateProviderClass(), BeanUtil::buildByEmptyArgs);
    }

}
