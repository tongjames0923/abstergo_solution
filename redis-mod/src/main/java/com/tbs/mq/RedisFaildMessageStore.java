package com.tbs.mq;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.mq.api.retry.AbstractFailedMessageStore;
import com.tbs.mq.model.FailedMessage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Redis实现的失败消息存储
 * <p>
 * 仅承担存储职责（保存/取出到期记录/删除），不包含任何调度逻辑；
 * 到期消息的重投递调度由mq-api的调度驱动（内嵌调度器或后台任务桥接）统一完成。
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public class RedisFaildMessageStore extends AbstractFailedMessageStore {

    @Setter
    private RedissonClient client;

    public RedisFaildMessageStore(RedissonClient client) {
        this.client = client;
    }

    public RedisFaildMessageStore() {
    }

    public RedissonClient getClient() {
        AssertUtil.notNullWithLevel(client, ErrorLevelEnum.CRITICAL_SYSTEM_ERROR, "RedissonClient cannot be null");
        return client;
    }

    /**
     * 实际的保存动作
     *
     * @param message 校验通过的失败消息记录
     */
    @Override
    protected void doSave(FailedMessage message) {
        RedissonClient redisson = getClient();

        // 1. 保存消息内容到 RMap
        String messageKey = "failed:message:" + message.getId();
        RMap<String, FailedMessage> messageMap = redisson.getMap(messageKey);
        messageMap.put(message.getId(), message);

        // 2. 添加到到期时间索引
        String indexKey = "failed:message:index";
        RScoredSortedSet<String> indexSet = redisson.getScoredSortedSet(indexKey);
        indexSet.add(message.getNextRetryTime().getTime(), message.getId());

        log.debug("Saved failed message {} with next retry time {}", message.getId(), message.getNextRetryTime());
    }

    /**
     * 实际的取出到期记录动作
     * <p>
     * 实现必须保证"取出即锁定"的原子语义。
     * </p>
     *
     * @param now   到期判定时间点
     * @param limit 单次最大取出条数
     * @return 已到期的失败消息记录列表
     */
    @Override
    protected List<FailedMessage> doFetchDue(Date now, int limit) {
        RedissonClient redisson = getClient();
        String indexKey = "failed:message:index";
        String statusKey = "failed:message:status";

        // Lua脚本：原子地取出到期消息并标记为处理中
        String fetchScript = "local indexKey = KEYS[1] " +
            "local statusKey = KEYS[2] " +
            "local now = tonumber(ARGV[1]) " +
            "local limit = tonumber(ARGV[2]) " +
            "local result = {} " +
            " " +
            "-- 获取到期的消息ID " +
            "local messageIds = redis.call('ZRANGEBYSCORE', indexKey, '-inf', now, 'LIMIT', 0, limit) " +
            " " +
            "for i, messageId in ipairs(messageIds) do " +
            "    -- 标记为处理中 " +
            "    redis.call('HSET', statusKey, messageId, 'PROCESSING') " +
            "    -- 从索引中移除 " +
            "    redis.call('ZREM', indexKey, messageId) " +
            "    table.insert(result, messageId) " +
            "end " +
            " " +
            "return result";

        RScript script = redisson.getScript();
        List<String> messageIds = script.eval(RScript.Mode.READ_WRITE, fetchScript, RScript.ReturnType.MULTI,
            Arrays.asList(indexKey, statusKey), String.valueOf(now.getTime()), String.valueOf(limit));

        List<FailedMessage> result = new ArrayList<>();
        if (CollUtil.isEmpty(result)) {
            return List.of();
        }

        // 获取消息内容
        for (String messageId : messageIds) {
            String messageKey = "failed:message:" + messageId;
            RMap<String, FailedMessage> messageMap = redisson.getMap(messageKey);
            FailedMessage message = messageMap.get(messageId);
            if (message != null) {
                result.add(message);
            } else {
                log.warn("Message {} found in index but not in message store", messageId);
                // 清理状态
                redisson.getMap(statusKey).remove(messageId);
            }
        }

        log.debug("Fetched {} due messages", result.size());
        return result;
    }

    /**
     * 实际的删除动作
     *
     * @param messageId 记录唯一标识
     */
    @Override
    protected void doRemove(String messageId) {
        RedissonClient redisson = getClient();

        // 1. 删除消息内容
        String messageKey = "failed:message:" + messageId;
        RMap<String, FailedMessage> messageMap = redisson.getMap(messageKey);
        messageMap.remove(messageId);

        // 2. 删除状态记录
        String statusKey = "failed:message:status";
        redisson.getMap(statusKey).remove(messageId);

        // 3. 删除索引（如果存在）
        String indexKey = "failed:message:index";
        RScoredSortedSet<String> indexSet = redisson.getScoredSortedSet(indexKey);
        indexSet.remove(messageId);

        log.debug("Removed failed message {}", messageId);
    }
}
