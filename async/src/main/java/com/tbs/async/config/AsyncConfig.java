package com.tbs.async.config;

import com.tbs.async.config.property.ExecutorProperty;
import com.tbs.async.constant.AsyncBeanNameConstant;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.common.context.ContextPropagator;
import com.tbs.common.delegate.provider.IDelegateFlagProvider;
import com.tbs.thread.IThreadPoolManager;
import com.tbs.thread.impl.DefaultMultiLayerThreadPoolManager;
import com.tbs.thread.impl.LongTermThreadExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;

/**
 * 异步功能框架配置类
 * <p>
 * 配置异步执行所需的线程池、执行器和相关组件
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    @Resource
    private ExecutorProperty executorProperty;

    /**
     * 创建主执行器服务
     * <p>
     * 创建并配置主线程池执行器，用于处理常规异步任务
     * </p>
     *
     * @param customizableThreadFactory        线程工厂
     * @param rejectedExecutionHandler 拒绝策略处理器
     * @return 配置好的线程池执行器
     */
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

    /**
     * 创建长期执行器服务
     * <p>
     * 创建并配置长期运行的线程执行器，用于处理需要长时间运行的异步任务
     * </p>
     *
     * @return 长期运行的线程执行器
     */
    @Bean(AsyncBeanNameConstant.LONG_TERM_EXECUTOR_SERVICE)
    public Executor longTermExecutorService() {
        return new LongTermThreadExecutor();
    }

    /**
     * 创建主执行器线程工厂
     * <p>
     * 创建用于生成主执行器线程的线程工厂
     * </p>
     *
     * @return 配置好的线程工厂
     */
    @Bean(AsyncBeanNameConstant.MAIN_EXECUTOR_SERVICE_THREAD_FACTORY)
    public ThreadFactory mainExecutorServiceThreadFactory() {
        return new CustomizableThreadFactory(executorProperty.getMainExecutorServiceThreadNamePrefix());
    }

    /**
     * 创建主执行器拒绝策略处理器
     * <p>
     * 创建用于处理主执行器线程拒绝策略的处理器
     * </p>
     *
     * @return 拒绝策略处理器（使用CallerRunsPolicy）
     */
    @Bean(AsyncBeanNameConstant.MAIN_EXECUTOR_SERVICE_REJECTED_EXECUTION_HANDLER)
    public RejectedExecutionHandler mainExecutorServiceRejectedExecutionHandler() {
        return new ThreadPoolExecutor.CallerRunsPolicy();
    }

    /**
     * 创建异步委托调用器
     * <p>
     * 创建用于异步调用委托方法的调用器
     * </p>
     *
     * @param delegateFlagProvider 委托标志提供者
     * @return 异步委托调用器
     */
    @Bean
    @Scope("prototype")
    public AsyncDelegateInvoker asyncDelegateInvoker(IDelegateFlagProvider delegateFlagProvider) {
        return new AsyncDelegateInvoker(delegateFlagProvider);
    }

    /**
     * 创建线程池管理器
     * <p>
     * 创建用于管理线程池的管理器，当没有自定义实现时使用默认实现
     * </p>
     *
     * @param executorService          主执行器服务
     * @param longTermExecutorService 长期执行器服务
     * @return 线程池管理器
     */
    @Bean
    @ConditionalOnMissingBean(IThreadPoolManager.class)
    public IThreadPoolManager threadPoolManager(
        @Qualifier(AsyncBeanNameConstant.MAIN_EXECUTOR_SERVICE) ExecutorService executorService,
        @Qualifier(AsyncBeanNameConstant.LONG_TERM_EXECUTOR_SERVICE) Executor longTermExecutorService,
        @Autowired(required = false) List<ContextPropagator> propagators) {
        DefaultMultiLayerThreadPoolManager manager = new DefaultMultiLayerThreadPoolManager(
            new DefaultMultiLayerThreadPoolManager.ExecutorInfo(executorService,
                AsyncBeanNameConstant.MAIN_EXECUTOR_SERVICE, 5000, 0),
            new DefaultMultiLayerThreadPoolManager.ExecutorInfo(longTermExecutorService,
                AsyncBeanNameConstant.LONG_TERM_EXECUTOR_SERVICE, 5 * 60 * 1000, 100));
        // 注入上下文传播器，用于在异步任务中传播父线程的上下文（如 RequestDataContext、Locale）
        manager.setPropagators(propagators);
        return manager;
    }

}
