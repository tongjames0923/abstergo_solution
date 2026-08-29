package com.tbs.schedule;

import com.tbs.common.util.base.CollUtil;
import com.tbs.schedule.api.TaskStore;
import com.tbs.schedule.api.TaskSubmission;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class RedisTaskStore implements TaskStore {

    @Resource
    private RedissonClient redissonClient;

    private static final String bucketName = "schedule:task";

    /**
     * 登记任务
     *
     * @param submission 任务
     */
    @Override
    public void save(TaskSubmission submission) {
        if (submission.isPersistable()) {
            redissonClient.getMap(bucketName).put(submission.getTaskId(), submission);
            return;
        }
        log.warn("task is not persistable, skip save to redis: {}", submission.getTaskId());
    }

    /**
     * 按标识查询任务
     *
     * @param taskId 任务标识
     * @return 任务（不存在返回空）
     */
    @Override
    public Optional<TaskSubmission> find(String taskId) {
        var task = redissonClient.getMap(bucketName).get(taskId);
        if (task == null) {
            return Optional.empty();
        }
        return Optional.of((TaskSubmission)task);
    }

    /**
     * 标记任务已终结并移除
     *
     * @param taskId 任务标识
     */
    @Override
    public void remove(String taskId) {
        redissonClient.getMap(bucketName).remove(taskId);
    }

    /**
     * 取出所有未终结任务（用于启动时恢复）
     *
     * @return 未终结任务集合
     */
    @Override
    public Collection<TaskSubmission> loadUnfinished() {
        var values = redissonClient.getMap(bucketName).values();
        if (CollUtil.isEmpty(values)) {
            return Arrays.asList();
        }
        return values.stream().map(TaskSubmission.class::cast).collect(Collectors.toList());
    }
}
