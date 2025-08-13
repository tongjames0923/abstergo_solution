package com.tbs.lock.config;

import com.tbs.lock.ILockProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 锁属性
 *
 * @author abstergo
 */
@Component
@ConfigurationProperties(prefix = "tbs.lock")
@Data
public class LockProperty {

    /**
     * 锁提供者类型,默认jdk锁
     */
    private Class<? extends ILockProvider> lockProviderType = com.tbs.lock.impls.providers.JdkLockProvider.class;
}
