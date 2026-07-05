package com.tbs.trace.config;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.trace.aop.MethodTraceAop;
import com.tbs.trace.core.*;
import com.tbs.trace.impl.ImportedTraceInterceptor;
import com.tbs.trace.impl.SimpleMethodInvokeTreeProvider;
import com.tbs.trace.impl.SpringManagedTraceContext;
import com.tbs.trace.impl.UuidTraceObjectGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class TraceConfig implements WebMvcConfigurer {

    @Resource
    private TraceProperty traceProperty;

    @Bean
    public AbstractTraceContext traceContext() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new SpringManagedTraceContext();
        }, traceProperty.getTraceContext(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(value = ITraceIdObjectGenerator.class)
    public ITraceIdObjectGenerator traceIdObjectGenerator() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new UuidTraceObjectGenerator();
        }, traceProperty.getTraceIdObjectGenerator(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    @ConditionalOnMissingBean(AbstractTraceInterceptor.class)
    AbstractTraceInterceptor traceInterceptor(AbstractTraceContext context) {
        return new ImportedTraceInterceptor(context);
    }

    @Bean
    @ConditionalOnMissingBean(IMethodInvokeTreeProvider.class)
    public IMethodInvokeTreeProvider methodInvokeTreeProvider() {
        return BeanUtil.buildBeanFromProperties(() -> {
            return new SimpleMethodInvokeTreeProvider();
        }, traceProperty.getMethodInvokeTreeProvider(), BeanUtil::buildByEmptyArgs);
    }

    @Bean
    MethodTraceAop aop() {
        return new MethodTraceAop();
    }

    @Resource
    @Lazy
    private AbstractTraceInterceptor traceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(traceInterceptor).excludePathPatterns(traceProperty.getExcludePaths());
        WebMvcConfigurer.super.addInterceptors(registry);
    }
}
