package com.tbs.mq.api.idempotent;

import java.time.Duration;

/**
 * 消息幂等存储契约
 * <p>
 * 幂等键采用三态生命周期，实现必须保证三个操作的分布式安全：
 * <ul>
 *     <li>占位（PROCESSING）：{@link #tryAcquire(String, Duration)} 互斥占位成功</li>
 *     <li>完成（DONE）：{@link #markDone(String, Duration)} 处理成功后固化，重复消息据此被拦截</li>
 *     <li>释放：{@link #release(String)} 处理失败后释放占位，允许重试再次进入</li>
 * </ul>
 * TTL必须显著大于消息的最大重试窗口，避免"占位成功后进程崩溃"导致幂等键永久卡死；
 * 到期自动放行产生的重复投递，由消费端业务幂等兜底。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public interface IIdempotentStore {

    /**
     * 尝试互斥占位
     * <p>
     * 仅当幂等键不存在（或已过期）时占位成功。
     * 键已存在时无论处于占位态还是完成态，均返回false。
     * </p>
     *
     * @param key 幂等键（建议含队列名前缀以避免跨队列碰撞）
     * @param ttl 占位有效期
     * @return true表示占位成功，可以执行处理逻辑；false表示消息已被处理或正在被处理
     */
    boolean tryAcquire(String key, Duration ttl);

    /**
     * 标记处理完成
     * <p>
     * 处理成功后调用，将幂等键固化为完成态并延续至指定TTL。
     * 此后的重复投递将被{@link #tryAcquire(String, Duration)}拦截。
     * </p>
     *
     * @param key 幂等键
     * @param ttl 完成态保留时长
     */
    void markDone(String key, Duration ttl);

    /**
     * 释放占位
     * <p>
     * 处理失败后调用，删除占位记录，使后续重投递可以再次通过
     * {@link #tryAcquire(String, Duration)}进入处理流程。
     * 实现必须保证仅删除处于占位态的记录，不得误删完成态记录。
     * </p>
     *
     * @param key 幂等键
     */
    void release(String key);

}
