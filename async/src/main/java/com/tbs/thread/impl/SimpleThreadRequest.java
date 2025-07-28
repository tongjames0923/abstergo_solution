package com.tbs.thread.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.thread.IThreadRequest;
import org.jetbrains.annotations.NotNull;

/**
 * 线程请求实现类
 *
 * @author tongj
 */
public class SimpleThreadRequest implements IThreadRequest {

    private String name;
    private int priority;
    private int expectedCostTime;
    private String expectedExecutorServiceName;

    private IThreadCall threadCall;

    public static final class Builder {
        private String name;
        private IThreadCall threadCall;
        private Integer priority;
        private Integer expectedCostTime;
        private String expectedExecutorServiceName;

        private Builder() {
        }

        public static final Builder create(@NotNull String name, @NotNull IThreadCall threadCall) {
            AssertUtil.hasText(name, "name can not be null or empty");
            AssertUtil.notNull(threadCall, "threadCall can not be null");
            Builder builder = new Builder();
            builder.name = name;
            builder.threadCall = threadCall;
            return builder;
        }

        public static final Builder create(@NotNull String name, @NotNull Runnable threadCall) {
            AssertUtil.hasText(name, "name can not be null or empty");
            AssertUtil.notNull(threadCall, "threadCall can not be null");
            Builder builder = new Builder();
            builder.name = name;
            builder.threadCall = new DefaultThreadCall(threadCall);
            return builder;
        }

        public Builder withPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder withExpectedCostTime(int expectedCostTime) {
            this.expectedCostTime = expectedCostTime;
            return this;
        }

        public Builder withExpectedExecutorServiceName(String expectedExecutorServiceName) {
            this.expectedExecutorServiceName = expectedExecutorServiceName;
            return this;
        }

        public SimpleThreadRequest build() {
            return new SimpleThreadRequest(name, threadCall, priority, expectedCostTime, expectedExecutorServiceName);
        }
    }

    public SimpleThreadRequest(@NotNull String name, @NotNull IThreadCall threadCall, Integer priority,
        Integer expectedCostTime, String expectedExecutorServiceName) {
        this.threadCall = threadCall;
        this.name = name;
        this.priority = priority == null ? 0 : priority;
        this.expectedCostTime = expectedCostTime == null ? 0 : expectedCostTime;
        this.expectedExecutorServiceName = expectedExecutorServiceName;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public int getExpectedCostTime() {
        return expectedCostTime;
    }

    @Override
    public String getExpectedExecutorServiceName() {
        return expectedExecutorServiceName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IThreadCall getThreadCall() {
        return threadCall;
    }

}
