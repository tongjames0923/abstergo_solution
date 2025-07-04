package com.tbs.async.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.DefaultManagedAwareThreadFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author tongj
 */
@Data
@ConfigurationProperties("tbs.async.executor")
public class ExecutorProperty {
    private String mainExecutorServiceName = "mainExecutorService";
    private String mainExecutorServiceThreadNamePrefix = "mainExecutorService-";
    private int mainExecutorServiceCorePoolSize = 10;
    private int mainExecutorServiceMaxPoolSize = 100;
    private int mainExecutorServiceQueueCapacity = 100;
    private int mainExecutorServiceKeepAliveSeconds = 60;

    private Class<? extends RejectedExecutionHandler> mainExecutorServiceUncaughtExceptionHandler =
        ThreadPoolExecutor.CallerRunsPolicy.class;
    private Class<? extends ThreadFactory> mainExecutorServiceThreadFactory = CustomizableThreadFactory.class;
    
}
