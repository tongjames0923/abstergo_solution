package com.tbs.common.config.property;

import com.tbs.common.delegate.provider.IDelegateFlagProvider;
import com.tbs.common.delegate.provider.IDelegateInvokerProvider;
import com.tbs.common.delegate.provider.IDelegateTargetProvider;
import com.tbs.common.delegate.provider.impl.CompletableFutureFlagProvider;
import com.tbs.common.delegate.provider.impl.SimpleCallerDelegateTargetProvider;
import com.tbs.common.delegate.provider.impl.SpringManagedTypeBaseInvokerProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 代理属性
 *
 * @author tongj
 */
@Data
@Component
@ConfigurationProperties("tbs.delegate")
public class DelegateProperty {
    /**
     * 代理目标提供者
     */
    private Class<? extends IDelegateTargetProvider> delegateTargetProvider = SimpleCallerDelegateTargetProvider.class;

    /**
     * 代理调用标记提供者
     */
    private Class<? extends IDelegateFlagProvider> delegateFlagProvider = CompletableFutureFlagProvider.class;
}
