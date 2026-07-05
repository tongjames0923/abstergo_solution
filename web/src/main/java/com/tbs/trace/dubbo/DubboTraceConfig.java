package com.tbs.trace.dubbo;

import com.tbs.trace.core.AbstractTraceContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    /**
     * 注册RpcContext TraceId对象查找者
     *
     * @return RpcContextTraceIdObjectFounder实例
     */
    @Bean
    public RpcContextTraceIdObjectFounder rpcContextTraceIdObjectFounder() {
        return new RpcContextTraceIdObjectFounder();
    }

    /**
     * 注册Dubbo Consumer端Trace过滤器
     *
     * @param traceContext trace上下文
     * @return DubboTraceConsumerFilter实例
     */
    @Bean
    public DubboTraceConsumerFilter dubboTraceConsumerFilter(AbstractTraceContext traceContext) {
        DubboTraceConsumerFilter filter = new DubboTraceConsumerFilter();
        filter.setTraceContext(traceContext);
        return filter;
    }

    /**
     * 注册Dubbo Provider端Trace过滤器
     *
     * @param traceContext trace上下文
     * @return DubboTraceProviderFilter实例
     */
    @Bean
    public DubboTraceProviderFilter dubboTraceProviderFilter(AbstractTraceContext traceContext) {
        DubboTraceProviderFilter filter = new DubboTraceProviderFilter();
        filter.setTraceContext(traceContext);
        return filter;
    }
}