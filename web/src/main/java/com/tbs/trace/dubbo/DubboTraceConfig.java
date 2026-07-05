package com.tbs.trace.dubbo;

import com.tbs.trace.config.TraceProperty;
import com.tbs.trace.core.AbstractTraceContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * Dubbo Trace配置类
 * <p>
 * 注册Dubbo Trace相关的Bean
 * </p>
 *
 * @author abstergo
 */
@Configuration
@ConditionalOnProperty(prefix = "tbs.trace", name = "dubbo-trace-enabled", havingValue = "true", matchIfMissing = true)
public class DubboTraceConfig {

    @Resource
    private TraceProperty traceProperty;

    @Bean
    public RpcContextTraceIdObjectFounder rpcContextTraceIdObjectFounder() {
        return new RpcContextTraceIdObjectFounder();
    }

    @Bean
    public DubboTraceConsumerFilter dubboTraceConsumerFilter(AbstractTraceContext traceContext) {
        DubboTraceConsumerFilter filter = new DubboTraceConsumerFilter();
        filter.setTraceContext(traceContext);
        return filter;
    }

    @Bean
    public DubboTraceProviderFilter dubboTraceProviderFilter(AbstractTraceContext traceContext) {
        DubboTraceProviderFilter filter = new DubboTraceProviderFilter();
        filter.setTraceContext(traceContext);
        filter.setTraceProperty(traceProperty);
        return filter;
    }
}
