package com.tbs.schedule.executor;

import com.tbs.schedule.api.ExecutionPolicy;
import com.tbs.schedule.api.TaskExecuteErrorHandle;
import com.tbs.schedule.impl.SimpleLogErrorHandle;

import java.time.Duration;

/**
 * 全局默认执行策略
 * <p>
 * 提供兜底的执行策略：不限制超时、默认隔离分组、普通优先级、默认仅打日志的失败处理器。 由框架经 {@code @ConditionalOnMissingBean} 注册，业务方可注册自定义
 * {@link ExecutionPolicy} 覆盖。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DefaultExecutionPolicy implements ExecutionPolicy {

    /**
     * 默认隔离分组
     */
    public static final String DEFAULT_GROUP = "default";

    private final Duration timeout;

    /**
     * 构造默认执行策略（不限制超时）
     */
    public DefaultExecutionPolicy() {
        this.timeout = null;
    }

    @Override
    public Duration getTimeout() {
        return timeout;
    }

    @Override
    public String getIsolationGroup() {
        return DEFAULT_GROUP;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public TaskExecuteErrorHandle getErrorHandle() {
        return new SimpleLogErrorHandle();
    }
}
