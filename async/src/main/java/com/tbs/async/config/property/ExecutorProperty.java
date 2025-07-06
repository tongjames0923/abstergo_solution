package com.tbs.async.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步执行器配置属性类
 * 用于配置和管理线程池的相关属性
 *
 * @author tongj
 */
@Data
@ConfigurationProperties("tbs.async.executor")
@Component
public class ExecutorProperty {
    /**
     * 主执行器服务的名称 默认值："mainExecutorService"
     */
    private String mainExecutorServiceName = "mainExecutorService";

    /**
     * 主执行器服务线程的名称前缀 默认值："mainExecutorService-"
     */
    private String mainExecutorServiceThreadNamePrefix = "mainExecutorService-";

    /**
     * 主执行器服务的核心线程池大小
     * 默认值：10
     */
    private int mainExecutorServiceCorePoolSize = 10;

    /**
     * 主执行器服务的最大线程池大小
     * 默认值：100
     */
    private int mainExecutorServiceMaxPoolSize = 100;

    /**
     * 主执行器服务的任务队列容量
     * 默认值：100
     */
    private int mainExecutorServiceQueueCapacity = 100;

    /**
     * 主执行器服务线程空闲超时时间（秒）
     * 默认值：60
     */
    private int mainExecutorServiceKeepAliveSeconds = 60;

    /**
     * 主执行器服务的未捕获异常处理器
     * 默认值：ThreadPoolExecutor.CallerRunsPolicy.class
     */
    private Class<? extends RejectedExecutionHandler> mainExecutorServiceUncaughtExceptionHandler =
        ThreadPoolExecutor.CallerRunsPolicy.class;

    /**
     * 主执行器服务的线程工厂
     * 默认值：CustomizableThreadFactory.class
     */
    private Class<? extends ThreadFactory> mainExecutorServiceThreadFactory = CustomizableThreadFactory.class;

}
