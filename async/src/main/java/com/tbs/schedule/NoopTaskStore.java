package com.tbs.schedule;

import com.tbs.schedule.api.TaskStore;
import com.tbs.schedule.api.TaskSubmission;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存兜底任务存储（默认实现）
 * <p>
 * 不落盘，进程退出即丢失。适用于缓存 TTL 等"丢失无副作用"的轻场景，保证框架未配置外部存储时可直接启动。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class NoopTaskStore implements TaskStore {

    private final Map<String, TaskSubmission> store = new ConcurrentHashMap<>();

    @Override
    public void save(TaskSubmission submission) {
        store.put(submission.getTaskId(), submission);
    }

    @Override
    public Optional<TaskSubmission> find(String taskId) {
        return Optional.ofNullable(store.get(taskId));
    }

    @Override
    public void remove(String taskId) {
        store.remove(taskId);
    }

    @Override
    public Collection<TaskSubmission> loadUnfinished() {
        return Collections.unmodifiableCollection(store.values());
    }
}
