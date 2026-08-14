package com.tbs.mq;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.mq.api.retry.AbstractFailedMessageStore;
import com.tbs.mq.model.FailedMessage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.util.*;

/**
 * Redis实现的失败消息存储
 * <p>
 * 仅承担存储职责（保存/取出到期记录/删除），不包含任何调度逻辑；
 * 到期消息的重投递调度由mq-api的调度驱动（内嵌调度器或后台任务桥接）统一完成。
 * </p>
 * <p>
 * 所有Redis操作均通过Lua脚本+StringCodec执行，确保key编码方式统一，
 * 避免DefaultSystemCodec（GenericJackson2JsonRedisSerializer）对key添加JSON类型前缀导致的key不一致问题。
 * FailedMessage对象的序列化/反序列化在Java侧用ObjectMapper完成，Lua脚本只负责传递和存储原始字符串。
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public class RedisFaildMessageStore extends AbstractFailedMessageStore {

    private static final String INDEX_KEY = "failed:message:index";
    private static final String STATUS_KEY = "failed:message:status";
    private static final String MESSAGE_KEY_PREFIX = "failed:message:";

    private static final ObjectMapper objectMapper = new ObjectMapper();

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
     * <p>
     * 通过Lua脚本原子地写入消息内容和索引，使用StringCodec确保key编码一致。
     * </p>
     *
     * @param message 校验通过的失败消息记录
     */
    @Override
    protected void doSave(FailedMessage message) {
        RedissonClient redisson = getClient();
        String messageKey = MESSAGE_KEY_PREFIX + message.getId();
        long score = message.getNextRetryTime().getTime();

        String jsonData;
        try {
            jsonData = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("[doSave] 消息序列化失败, id={}", message.getId(), e);
            return;
        }

        // Lua脚本：原子地写入消息内容+索引
        String script = "local messageKey = KEYS[1] " +
            "local indexKey = KEYS[2] " +
            "local messageId = ARGV[1] " +
            "local jsonData = ARGV[2] " +
            "local score = tonumber(ARGV[3]) " +
            "redis.call('HSET', messageKey, messageId, jsonData) " +
            "redis.call('ZADD', indexKey, score, messageId) " +
            "return 'OK'";

        RScript rScript = redisson.getScript(StringCodec.INSTANCE);
        rScript.eval(RScript.Mode.READ_WRITE, script, RScript.ReturnType.VALUE, Arrays.asList(messageKey, INDEX_KEY),
            message.getId(), jsonData, String.valueOf(score));

        log.debug("[doSave] 消息已保存, id={}, score={}, queue={}, indexKey={}", message.getId(), score,
            message.getQueueName(), INDEX_KEY);
    }

    /**
     * 实际的取出到期记录动作
     * <p>
     * 通过Lua脚本原子地取出到期消息、标记为处理中、从索引移除，并返回消息ID列表（JSON数组）。
     * 使用cjson.encode返回结果（与诊断脚本一致，避免table.concat的返回值被解码为null）。
     * </p>
     *
     * @param now   到期判定时间点
     * @param limit 单次最大取出条数
     * @return 已到期的失败消息记录列表
     */
    @Override
    protected List<FailedMessage> doFetchDue(Date now, int limit) {
        RedissonClient redisson = getClient();
        long nowMillis = now.getTime();

        log.debug("[doFetchDue] 开始查询, now={}, limit={}", nowMillis, limit);

        // Lua脚本：原子地取出到期消息并标记为处理中，用cjson.encode返回JSON数组
        String fetchScript = "local indexKey = KEYS[1] " +
            "local statusKey = KEYS[2] " +
            "local now = tonumber(ARGV[1]) " +
            "local limit = tonumber(ARGV[2]) " +
            "local result = {} " +
            "local messageIds = redis.call('ZRANGEBYSCORE', indexKey, '-inf', now, 'LIMIT', 0, limit) " +
            "for i, messageId in ipairs(messageIds) do " +
            "    redis.call('HSET', statusKey, messageId, 'PROCESSING') " +
            "    redis.call('ZREM', indexKey, messageId) " +
            "    table.insert(result, messageId) " +
            "end " + "return cjson.encode(result)";

        RScript script = redisson.getScript(StringCodec.INSTANCE);
        String evalResult = script.eval(RScript.Mode.READ_WRITE, fetchScript, RScript.ReturnType.VALUE,
            Arrays.asList(INDEX_KEY, STATUS_KEY), String.valueOf(nowMillis), String.valueOf(limit));

        log.debug("[doFetchDue] Lua脚本返回, evalResult={}", evalResult);

        if (evalResult == null || evalResult.isEmpty() || "[]".equals(evalResult) || "{}".equals(evalResult)) {
            log.debug("[doFetchDue] 无到期消息");
            return List.of();
        }

        List<String> messageIds;
        try {
            messageIds = objectMapper.readValue(evalResult, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.error("[doFetchDue] 解析Lua返回值失败, evalResult={}", evalResult, e);
            return List.of();
        }

        log.debug("[doFetchDue] 取出{}条到期消息, ids={}", messageIds.size(), messageIds);

        // 逐条读取消息内容
        List<FailedMessage> result = new ArrayList<>();
        for (String messageId : messageIds) {
            String messageKey = MESSAGE_KEY_PREFIX + messageId;
            String json = hget(script, messageKey, messageId);
            if (json != null && !json.isEmpty()) {
                try {
                    FailedMessage message = objectMapper.readValue(json, FailedMessage.class);
                    result.add(message);
                    log.debug("[doFetchDue] 消息读取成功, id={}, queue={}", messageId, message.getQueueName());
                } catch (Exception e) {
                    log.error("[doFetchDue] 消息反序列化失败, id={}", messageId, e);
                }
            } else {
                log.warn("[doFetchDue] 消息内容不存在, id={}, 清理状态", messageId);
                hdel(script, STATUS_KEY, messageId);
            }
        }

        log.debug("[doFetchDue] 本轮共取出{}条到期消息", result.size());
        return result;
    }

    /**
     * 实际的删除动作
     * <p>
     * 通过Lua脚本原子地删除消息内容、状态记录和索引。
     * </p>
     *
     * @param messageId 记录唯一标识
     */
    @Override
    protected void doRemove(String messageId) {
        RedissonClient redisson = getClient();
        String messageKey = MESSAGE_KEY_PREFIX + messageId;

        // Lua脚本：原子地删除消息内容+状态+索引
        String script = "local messageKey = KEYS[1] " +
            "local indexKey = KEYS[2] " +
            "local statusKey = KEYS[3] " +
            "local messageId = ARGV[1] " +
            "redis.call('HDEL', messageKey, messageId) " +
            "redis.call('HDEL', statusKey, messageId) " +
            "redis.call('ZREM', indexKey, messageId) " +
            "return 'OK'";

        RScript rScript = redisson.getScript(StringCodec.INSTANCE);
        rScript.eval(RScript.Mode.READ_WRITE, script, RScript.ReturnType.VALUE,
            Arrays.asList(messageKey, INDEX_KEY, STATUS_KEY), messageId);

        log.debug("[doRemove] 消息已删除, id={}", messageId);
    }

    /**
     * 通过Lua脚本读取Hash字段值
     */
    private String hget(RScript script, String key, String field) {
        String lua = "return redis.call('HGET', KEYS[1], ARGV[1])";
        return script.eval(RScript.Mode.READ_ONLY, lua, RScript.ReturnType.VALUE, Collections.singletonList(key),
            field);
    }

    /**
     * 通过Lua脚本删除Hash字段
     */
    private void hdel(RScript script, String key, String field) {
        String lua = "redis.call('HDEL', KEYS[1], ARGV[1]) return 'OK'";
        script.eval(RScript.Mode.READ_WRITE, lua, RScript.ReturnType.VALUE, Collections.singletonList(key), field);
    }
}
