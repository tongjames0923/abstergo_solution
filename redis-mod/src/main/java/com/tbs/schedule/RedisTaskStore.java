package com.tbs.schedule;

import com.tbs.common.util.base.CollUtil;
import com.tbs.schedule.api.TaskStore;
import com.tbs.schedule.api.TaskSubmission;
import com.tbs.schedule.impl.TaskSubmissionTransformer;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Redis 实现的任务持久化存储
 * <p>
 * 任务提交描述经 {@link TaskSubmissionTransformer}（{@code ITransformer} 数据转换 SPI）序列化为 JSON 字符串后 存入 Redis Hash：key 与 value 均使用
 * {@link StringCodec} 编码，保证键编码统一且存储内容可读。 进程重启后经 {@link #loadUnfinished()} 逐条还原恢复；单条损坏数据记错误日志并跳过，不阻断整体恢复。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class RedisTaskStore implements TaskStore {

    /**
     * 任务桶名称（冒号分层键）
     */
    private static final String BUCKET_NAME = "schedule:task";

    /**
     * 坏数据日志摘要最大长度
     */
    private static final int SNIPPET_MAX_LENGTH = 128;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 任务提交描述转换器（无状态线程安全，直接持有复用）
     */
    private final TaskSubmissionTransformer transformer = new TaskSubmissionTransformer();

    /**
     * 登记任务
     * <p>
     * 可持久化任务序列化为 JSON 快照后写入 Redis；内存态载荷（不可持久化）跳过落盘并记告警日志。
     * </p>
     *
     * @param submission 任务
     */
    @Override
    public void save(TaskSubmission submission) {
        if (submission.isPersistable()) {
            taskBucket().put(submission.getTaskId(),
                new String(transformer.transform(submission), StandardCharsets.UTF_8));
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
        String json = taskBucket().get(taskId);
        if (json == null || json.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(reverse(json));
    }

    /**
     * 标记任务已终结并移除
     *
     * @param taskId 任务标识
     */
    @Override
    public void remove(String taskId) {
        taskBucket().remove(taskId);
    }

    /**
     * 取出所有未终结任务（用于启动时恢复）
     * <p>
     * 逐条容错：单条损坏数据记错误日志（仅截断摘要，避免日志膨胀）并跳过，不阻断整体恢复。
     * </p>
     *
     * @return 未终结任务集合
     */
    @Override
    public Collection<TaskSubmission> loadUnfinished() {
        Collection<String> values = taskBucket().values();
        if (CollUtil.isEmpty(values)) {
            return Arrays.asList();
        }
        List<TaskSubmission> tasks = new ArrayList<>(values.size());
        for (String json : values) {
            try {
                tasks.add(reverse(json));
            } catch (Exception e) {
                log.error("task snapshot restore failed, skip it: preview={}", truncate(json), e);
            }
        }
        return tasks;
    }

    /**
     * 获取任务桶（StringCodec 编码的 Redis Hash）
     *
     * @return 任务桶
     */
    private RMap<String, String> taskBucket() {
        return redissonClient.getMap(BUCKET_NAME, StringCodec.INSTANCE);
    }

    /**
     * 将 JSON 快照还原为任务提交描述
     *
     * @param json JSON 快照
     * @return 任务提交描述
     */
    private TaskSubmission reverse(String json) {
        return transformer.reverseTransform(json.getBytes(StandardCharsets.UTF_8), TaskSubmission.class);
    }

    /**
     * 截断快照内容用于日志展示
     *
     * @param json JSON 快照
     * @return 截断后的摘要
     */
    private String truncate(String json) {
        return json.length() <= SNIPPET_MAX_LENGTH ? json : json.substring(0, SNIPPET_MAX_LENGTH) + "...";
    }
}
