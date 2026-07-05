package com.tbs.trace.config;

import com.tbs.trace.core.AbstractTraceContext;
import com.tbs.trace.core.IMethodInvokeTreeProvider;
import com.tbs.trace.core.ITraceIdObjectGenerator;
import com.tbs.trace.impl.SimpleMethodInvokeTreeProvider;
import com.tbs.trace.impl.SpringManagedTraceContext;
import com.tbs.trace.impl.UuidTraceObjectGenerator;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 链路追踪属性
 *
 * @author abstergo
 */
@Data
@ConfigurationProperties(prefix = "tbs.trace")
@Component
public class TraceProperty {
    /**
     * 链路追踪对象生成器
     */
    private Class<? extends ITraceIdObjectGenerator> traceIdObjectGenerator = UuidTraceObjectGenerator.class;

    /**
     * 链路追踪上下文
     */
    private Class<? extends AbstractTraceContext> traceContext = SpringManagedTraceContext.class;

    /**
     * 链路追踪排除路径
     */
    private String[] excludePaths = new String[0];

    /**
     * 函数链路追踪提供者
     */
    private Class<? extends IMethodInvokeTreeProvider> methodInvokeTreeProvider = SimpleMethodInvokeTreeProvider.class;

    /**
     * 是否启用Dubbo链路追踪
     */
    private boolean dubboTraceEnabled = true;

    /**
     * 是否启用分布式链路追踪（需要RabbitMQ和Redis支持）
     */
    private boolean distributedTraceEnabled = true;
}
