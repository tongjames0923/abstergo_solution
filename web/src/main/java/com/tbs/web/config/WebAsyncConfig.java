package com.tbs.web.config;

import com.tbs.common.context.ContextPropagator;
import com.tbs.common.context.ContextPropagators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Spring MVC 异步支持配置
 * <p>
 * 将 Spring MVC 异步请求（Controller 返回 {@code Callable}、
 * {@code StreamingResponseBody} 等）的默认执行器接入 {@link ContextPropagator}
 * 传播链。未配置本类时，MVC 异步任务由 Spring 默认的
 * {@code SimpleAsyncTaskExecutor} 执行（每任务新建线程、不经任何上下文传播器），
 * 异步执行阶段无法读取请求上下文与语言上下文。
 * </p>
 * <p>
 * <b>背景</b>：异步请求中 {@code RequestContextLifecycleFilter} 的 finally 清理
 * 在请求挂起（async start）时即执行，异步执行线程依赖传播器快照获取上下文，
 * 本配置是 {@code Callable} 类异步路径的唯一上下文来源。
 * </p>
 * <p>
 * <b>覆盖范围说明</b>：本配置覆盖 MVC 框架管理的异步任务；{@code DeferredResult}
 * 的结果产生线程由业务代码决定——使用托管线程池（{@code IThreadPoolManager}）
 * 或 {@code @Async} 时已由传播链覆盖，使用
 * {@code CompletableFuture.supplyAsync()} 等默认线程池时<b>无上下文</b>，需显式传参。
 * </p>
 *
 * @author abstergo
 * @see WebMvcConfigurer
 * @see ContextPropagators
 */
@Configuration
public class WebAsyncConfig implements WebMvcConfigurer {

    /**
     * 上下文传播器列表，Spring 自动收集全部实现，允许为空
     */
    private final List<ContextPropagator> propagators;

    /**
     * 构造配置
     *
     * @param propagators 上下文传播器列表，允许为空
     */
    public WebAsyncConfig(@Autowired(required = false) List<ContextPropagator> propagators) {
        this.propagators = propagators;
    }

    /**
     * 配置 MVC 异步执行器
     * <p>
     * 继承 {@code SimpleAsyncTaskExecutor}（保持 Spring 默认的线程策略），仅覆写
     * {@code execute(Runnable, long)}——该方法是所有提交路径的单一汇聚点
     * （{@code execute(Runnable)}、{@code submit(Runnable)}、{@code submit(Callable)}
     * 均委托至此），在此经上下文传播器链包装任务，每个任务只被包装一次。
     * </p>
     *
     * @param configurer 异步支持配置器
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        AsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("mvc-async-") {
            @Override
            public void execute(Runnable task, long startTimeout) {
                super.execute(ContextPropagators.wrap(propagators, task), startTimeout);
            }
        };
        configurer.setTaskExecutor(executor);
    }
}
