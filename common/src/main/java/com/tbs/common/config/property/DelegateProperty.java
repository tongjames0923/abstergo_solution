package com.tbs.common.config.property;

import com.tbs.common.delegate.IDelegateProvider;
import com.tbs.common.delegate.impl.MainThreadCallDelegateProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DelegateProperty
 *
 * @author abstergo
 */
@Data
@Component
@ConfigurationProperties("tbs.delegate")
public class DelegateProperty {

    /**
     * 代理提供者
     */
    private Class<? extends IDelegateProvider> delegateProviderClass = MainThreadCallDelegateProvider.class;
}
