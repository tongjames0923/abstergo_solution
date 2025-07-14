package com.tbs.web.trace.config;

import com.tbs.common.util.BeanUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.web.trace.core.ITraceIdObjectFounder;
import com.tbs.web.trace.core.ITraceIdObjectGenerator;
import com.tbs.web.trace.core.TraceObject;
import com.tbs.web.trace.impl.UuidTraceObjectGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

/**
 * TraceId配置类
 *
 * @author abstergo
 */
@Configuration
public class TraceConfig {

    @Resource
    private TraceProperty traceProperty;

    @Lazy
    @Bean
    @RequestScope
    public TraceObject traceObject(ITraceIdObjectGenerator traceIdObjectGenerator,
        Map<String, ITraceIdObjectFounder> founderMap, HttpServletRequest request) {
        if (CollUtil.isNotEmpty(founderMap)) {
            for (ITraceIdObjectFounder iTraceIdObjectFounder : founderMap.values()) {
                Optional<TraceObject> traceObject = iTraceIdObjectFounder.findTraceObject(request);
                if (traceObject.isPresent()) {
                    return traceObject.get();
                }
            }
        }
        return traceIdObjectGenerator.generateTraceObject(request);
    }

    @Bean
    @ConditionalOnMissingBean(value = ITraceIdObjectGenerator.class)
    public ITraceIdObjectGenerator traceIdObjectGenerator() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new UuidTraceObjectGenerator();
        }, traceProperty.getTraceIdObjectGenerator(), BeanUtil::buildByEmptyArgs);
    }

}
