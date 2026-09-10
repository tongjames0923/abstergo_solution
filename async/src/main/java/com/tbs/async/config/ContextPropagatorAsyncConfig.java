package com.tbs.async.config;

import com.tbs.async.constant.AsyncBeanNameConstant;
import com.tbs.common.context.ContextPropagator;
import com.tbs.common.context.ContextPropagators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * Spring {@code @Async} 上下文传播配置
 * <p>
 * 将 Spring {@code @Async} 注解方法的默认执行器接入 {@link ContextPropagator}
 * 传播链。未配置本类时，{@code @Async} 回退到 Spring 默认的
 * {@code SimpleAsyncTaskExecutor}（每任务新建线程、不经任何上下文传播器），
 * 异步方法中无法读取请求上下文与语言上下文。
 * </p>
 * <p>
 * 配置后 {@code @Async} 方法统一提交到 {@link AsyncBeanNameConstant#MAIN_EXECUTOR_SERVICE}
 * 托管线程池执行（复用池化线程），并在提交时经传播器链快照、执行前恢复、
 * finally 恢复式清理上下文。
 * </p>
 *
 * @author abstergo
 * @see AsyncConfigurer
 * @see ContextPropagators
 * @see com.tbs.common.context.ContextPropagator
 */
@Configuration
public class ContextPropagatorAsyncConfig implements AsyncConfigurer {

    /**
     * 主执行器服务（AsyncConfig 装配的托管线程池）
     */
    private final ExecutorService mainExecutorService;

    /**
     * 上下文传播器列表，Spring 自动收集全部实现，允许为空
     */
    private final List<ContextPropagator> propagators;

    /**
     * 构造配置
     *
     * @param mainExecutorService 主执行器服务
     * @param propagators         上下文传播器列表，允许为空
     */
    public ContextPropagatorAsyncConfig(
        @Qualifier(AsyncBeanNameConstant.MAIN_EXECUTOR_SERVICE) ExecutorService mainExecutorService,
        @Autowired(required = false) List<ContextPropagator> propagators) {
        this.mainExecutorService = mainExecutorService;
        this.propagators = propagators;
    }

    /**
     * 获取 {@code @Async} 默认执行器
     * <p>
     * 返回经上下文传播器链包装的主执行器服务，传播器列表为空时原样返回。
     * </p>
     *
     * @return 包装后的执行器
     */
    @Override
    public Executor getAsyncExecutor() {
        return ContextPropagators.wrapExecutor(propagators, mainExecutorService);
    }
}
