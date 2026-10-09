package com.tbs.lock.config;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.lock.ILockProvider;
import com.tbs.lock.impls.providers.JdkLockProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

/**
 * @author abstergo
 */
@Configuration
public class LockConfig {
    @Resource
    private LockProperty lockProperty;

    /**
     * 锁提供者
     */
    @Bean
    ILockProvider lockProvider() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new JdkLockProvider();
        }, lockProperty.getLockProviderType(), BeanUtil::buildByEmptyArgs);
    }
}
