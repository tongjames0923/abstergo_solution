package com.tbs.web.trace.config;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.web.trace.aop.MethodTraceAop;
import com.tbs.web.trace.core.*;
import com.tbs.web.trace.impl.ImportedTraceInterceptor;
import com.tbs.web.trace.impl.SimpleMethodInvokeTreeProvider;
import com.tbs.web.trace.impl.SpringManagedTraceContext;
import com.tbs.web.trace.impl.UuidTraceObjectGenerator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;


public class TraceConfig implements WebMvcConfigurer {

    @Resource
    private TraceProperty traceProperty;

    @Lazy
    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    AbstractTraceContext traceContext() {
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
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    TraceObject traceObject(AbstractTraceContext context) {
        return context.getRootTrace();
    }

    @Bean
    @ConditionalOnMissingBean(IMethodInvokeTreeProvider.class)
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    IMethodInvokeTreeProvider methodInvokeTreeProvider() {
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
