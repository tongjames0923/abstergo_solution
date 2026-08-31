package com.tbs.cache.expire.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.schedule.api.TaskExecutor;
import com.tbs.schedule.api.TaskSubmission;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Date;

@Component
public class CacheExpireTaskExecutor implements TaskExecutor {

    /**
     * 构建任务提交
     *
     * @param cacheName 缓存名称
     * @param key       缓存key
     * @param triggerAt 触发时间
     * @return 任务提交
     */
    public static TaskSubmission build(String cacheName, Object key, Date triggerAt) {
        Serializable serializableKey = null;
        if (key == null) {
            serializableKey = (Serializable)null;
        } else {
            serializableKey = (Serializable)key;
        }
        return TaskSubmission.of(CacheExpireTaskPayload.TYPE,
            new CacheExpireTaskPayload(cacheName, serializableKey, null), triggerAt);
    }

    @Resource
    private CacheManager cacheManager;

    public void handle(CacheExpireTaskPayload payload) {
        if (payload.getKey() == null) {
            cacheManager.getCache(payload.getCacheName()).clear();
        } else {
            cacheManager.getCache(payload.getCacheName()).evict(payload.getKey());
        }
    }

    /**
     * 是否支持指定任务类型
     *
     * @param taskType 任务类型标识
     * @return 支持返回 true
     */
    @Override
    public boolean supports(String taskType) {
        return CacheExpireTaskPayload.TYPE.equals(taskType);
    }

    /**
     * 执行任务
     *
     * @param submission 任务提交描述
     */
    @Override
    public void execute(TaskSubmission submission) {
        AssertUtil.notNull(submission, "submission is null");
        AssertUtil.notNull(submission.getPayload(), "payload is null");
        handle((CacheExpireTaskPayload)submission.getPayload());
    }
}
