package com.tbs.web.trace.config;

import com.tbs.web.trace.core.AbstractTraceContext;
import com.tbs.web.trace.core.ITraceIdObjectGenerator;
import com.tbs.web.trace.impl.SpringManagedTraceContext;
import com.tbs.web.trace.impl.UuidTraceObjectGenerator;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 链路追踪属性
 *
 * @author abstergo
 */
@Data
@Component
@ConfigurationProperties(prefix = "tbs.trace")
public class TraceProperty {
    /**
     * 链路追踪对象生成器
     */
    private Class<? extends ITraceIdObjectGenerator> traceIdObjectGenerator = UuidTraceObjectGenerator.class;

    /**
     * 链路追踪上下文
     */
    private Class<? extends AbstractTraceContext> traceContext = SpringManagedTraceContext.class;
}
