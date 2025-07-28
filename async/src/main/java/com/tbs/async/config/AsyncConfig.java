package com.tbs.async.config;

import com.tbs.async.config.property.ExecutorProperty;
import com.tbs.async.constant.AsyncBeanNameConstant;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.common.delegate.provider.IDelegateFlagProvider;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.thread.IThreadPoolManager;
import com.tbs.thread.impl.DefaultMultiLayerThreadPoolManager;
import com.tbs.thread.impl.LongTermThreadExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步功能框架配置类
 *
 * @author tongj
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    @Resource
    private ExecutorProperty executorProperty;

    @Primary
    @Bean(AsyncBeanNameConstant.MAIN_EXECUTOR_SERVICE)
    public ExecutorService mainExecutorService(
        @Qualifier(AsyncBeanNameConstant.MAIN_EXECUTOR_SERVICE_THREAD_FACTORY) ThreadFactory customizableThreadFactory,
        @Qualifier(
            AsyncBeanNameConstant.MAIN_EXECUTOR_SERVICE_REJECTED_EXECUTION_HANDLER) RejectedExecutionHandler rejectedExecutionHandler) {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(executorProperty.getMainExecutorServiceCorePoolSize(),
            executorProperty.getMainExecutorServiceMaxPoolSize(),
            executorProperty.getMainExecutorServiceKeepAliveSeconds(), TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(executorProperty.getMainExecutorServiceQueueCapacity()),
            customizableThreadFactory, rejectedExecutionHandler);
        return executor;
    }

    @Bean(AsyncBeanNameConstant.LONG_TERM_EXECUTOR_SERVICE)
    public Executor longTermExecutorService() {
        return new LongTermThreadExecutor();
    }

    @Bean(AsyncBeanNameConstant.MAIN_EXECUTOR_SERVICE_THREAD_FACTORY)
    public ThreadFactory mainExecutorServiceThreadFactory() {
        return new CustomizableThreadFactory(executorProperty.getMainExecutorServiceThreadNamePrefix());
    }

    @Bean(AsyncBeanNameConstant.MAIN_EXECUTOR_SERVICE_REJECTED_EXECUTION_HANDLER)
    public RejectedExecutionHandler mainExecutorServiceRejectedExecutionHandler() {
        return new ThreadPoolExecutor.CallerRunsPolicy();
    }

    @Bean
    @Scope("prototype")
    public AsyncDelegateInvoker asyncDelegateInvoker(IDelegateFlagProvider delegateFlagProvider) {
        return new AsyncDelegateInvoker(delegateFlagProvider);
    }

    @Bean
    @ConditionalOnMissingBean(IThreadPoolManager.class)
    public IThreadPoolManager threadPoolManager(
        @Qualifier(AsyncBeanNameConstant.MAIN_EXECUTOR_SERVICE) ExecutorService executorService,
        @Qualifier(AsyncBeanNameConstant.LONG_TERM_EXECUTOR_SERVICE) Executor longTermExecutorService) {
        return new DefaultMultiLayerThreadPoolManager(
            new DefaultMultiLayerThreadPoolManager.ExecutorInfo(executorService,
                AsyncBeanNameConstant.MAIN_EXECUTOR_SERVICE, 5000, 0),
            new DefaultMultiLayerThreadPoolManager.ExecutorInfo(longTermExecutorService,
                AsyncBeanNameConstant.LONG_TERM_EXECUTOR_SERVICE, 5 * 60 * 1000, 100));
    }

}
