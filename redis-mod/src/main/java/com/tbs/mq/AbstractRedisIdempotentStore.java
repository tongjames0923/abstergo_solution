package com.tbs.mq;

import com.tbs.mq.api.idempotent.AbstractIdempotentStore;
import org.redisson.api.RBucket;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.Collections;

/**
 * 幂等Redis存储抽象类
 * <p>
 * 基于RBucket落地幂等键三态生命周期，键的value即状态标识：
 * <ul>
 *     <li>占位态（PROCESSING）：{@link #tryAcquire(String, Duration)} 经SET NX EX单命令原子写入并附带TTL</li>
 *     <li>完成态（DONE）：{@link #markDone(String, Duration)} 覆盖状态值并延续TTL</li>
 *     <li>释放：{@link #release(String)} 仅条件删除占位态，绝不触碰完成态</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 */
public abstract class AbstractRedisIdempotentStore extends AbstractIdempotentStore {

    /**
     * 占位态状态值
     */
    private static final String STATE_PROCESSING = "PROCESSING";

    /**
     * 完成态状态值
     */
    private static final String STATE_DONE = "DONE";

    /**
     * 条件删除脚本：仅当键值等于占位态时删除，避免误删完成态
     */
    private static final String RELEASE_SCRIPT =
        "if redis.call('get', KEYS[1]) == ARGV[1] then "
            + "return redis.call('del', KEYS[1]) "
            + "else "
            + "return 0 "
            + "end";

    /**
     * 获取Redisson客户端
     *
     * @return Redisson客户端
     */
    protected abstract RedissonClient getRedissonClient();

    /**
     * 默认TTL
     * <p>
     * 实现方应保证该值显著大于消息的最大重试窗口， 建议取"最大重试次数×最大单次延迟"的3倍以上。
     * </p>
     *
     * @return 默认占位/完成态有效期
     */
    @Override
    protected Duration defaultTtl() {
        return Duration.ofHours(1);
    }

    /**
     * 尝试互斥占位
     * <p>
     * SET NX EX单命令原子完成：仅当幂等键不存在（或已过期）时写入占位态并附带TTL，
     * 避免"占位成功后进程崩溃导致键无TTL永久残留"。
     * 键已存在时无论处于占位态还是完成态，均返回false。
     * </p>
     *
     * @param key 幂等键（建议含队列名前缀以避免跨队列碰撞）
     * @param ttl 占位有效期
     * @return true表示占位成功，可以执行处理逻辑；false表示消息已被处理或正在被处理
     */
    @Override
    public boolean tryAcquire(String key, Duration ttl) {
        RBucket<String> bucket = getRedissonClient().getBucket(key);
        return bucket.setIfAbsent(STATE_PROCESSING, ttl);
    }

    /**
     * 标记处理完成
     * <p>
     * 处理成功后调用，将幂等键固化为完成态并延续至指定TTL。 此后的重复投递将被{@link #tryAcquire(String, Duration)}拦截。
     * </p>
     *
     * @param key 幂等键
     * @param ttl 完成态保留时长
     */
    @Override
    public void markDone(String key, Duration ttl) {
        RBucket<String> bucket = getRedissonClient().getBucket(key);
        bucket.set(STATE_DONE, ttl);
    }

    /**
     * 释放占位
     * <p>
     * 处理失败后调用，经Lua脚本原子地"仅当键值仍为占位态时删除"，使后续重投递可以再次通过
     * {@link #tryAcquire(String, Duration)}进入处理流程；完成态记录不会被误删。
     * 脚本取客户端默认codec，保证ARGV与存储值的序列化方式一致。
     * </p>
     *
     * @param key 幂等键
     */
    @Override
    public void release(String key) {
        RScript script = getRedissonClient().getScript();
        script.eval(RScript.Mode.READ_WRITE, RELEASE_SCRIPT, RScript.ReturnType.INTEGER,
            Collections.singletonList(key), STATE_PROCESSING);
    }
}
