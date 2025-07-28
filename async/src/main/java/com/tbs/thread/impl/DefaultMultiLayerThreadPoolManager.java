package com.tbs.thread.impl;

import com.tbs.common.util.CollUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.thread.AbstractThreadPoolMananger;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * 默认多级线程池管理器
 *
 * @author tongj
 */
public class DefaultMultiLayerThreadPoolManager extends AbstractThreadPoolMananger {
    @Data
    @AllArgsConstructor
    public static final class ExecutorInfo {
        /**
         * 线程池
         */
        private Executor executor;
        /**
         * 线程池名称
         */
        private String name;
        /**
         * 能接受的最大耗时
         */
        private int maxAcceptedCostTime;

        private int priority;
    }

    /**
     * 线程池信息
     */
    Map<String, ExecutorInfo> executorMap = new ConcurrentHashMap<>(8);

    private List<ExecutorInfo> executorInfos;

    private ExecutorInfo defaultExecutorInfo;

    public DefaultMultiLayerThreadPoolManager(@NotNull ExecutorInfo defaultExecutorInfo,
        ExecutorInfo... executorInfos) {
        this.defaultExecutorInfo = defaultExecutorInfo;
        executorMap.put(defaultExecutorInfo.getName(), defaultExecutorInfo);
        this.executorInfos = new ArrayList<>(CollUtil.isEmpty(executorInfos) ? 1 : executorInfos.length + 1);
        this.executorInfos.add(defaultExecutorInfo);
        if (CollUtil.isEmpty(executorInfos)) {
            return;
        }
        for (ExecutorInfo executorInfo : executorInfos) {
            executorMap.put(executorInfo.getName(), executorInfo);
            this.executorInfos.add(executorInfo);
        }
        this.executorInfos.sort((o1, o2) -> o1.getPriority() - o2.getPriority());
    }

    @Override
    protected Optional<Executor> getExecutorByName(String executorName) {
        if (StrUtil.isEmpty(executorName)) {
            return Optional.empty();
        }
        return Optional.ofNullable(executorMap.getOrDefault(executorName, null)).map(ExecutorInfo::getExecutor);
    }

    @Override
    protected Executor getDefaultExecutor() {
        return defaultExecutorInfo.getExecutor();
    }

    @Override
    protected Optional<Executor> getExecutorByCostAndPriority(int costTime, int priority) {
        return this.executorInfos.stream().filter(info -> info.getMaxAcceptedCostTime() >= costTime).findFirst()
            .map(ExecutorInfo::getExecutor);
    }
}
