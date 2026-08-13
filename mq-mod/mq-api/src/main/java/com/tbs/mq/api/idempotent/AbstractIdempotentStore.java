package com.tbs.mq.api.idempotent;

import com.tbs.common.util.base.StrUtil;

import java.time.Duration;

/**
 * 消息幂等存储基类
 * <p>
 * 提供两类基础封装：
 * <ul>
 *     <li>默认TTL重载：未指定TTL的占位/完成操作统一走{@link #defaultTtl()}，
 *         实现方只需声明一个默认值</li>
 *     <li>幂等键拼装：{@link #composeKey(String, String)}统一"作用域:业务键"格式，
 *         避免跨队列的幂等键碰撞</li>
 * </ul>
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public abstract class AbstractIdempotentStore implements IIdempotentStore {

    /**
     * 尝试互斥占位（使用默认TTL）
     *
     * @param key 幂等键
     * @return true表示占位成功
     */
    public boolean tryAcquire(String key) {
        return tryAcquire(key, defaultTtl());
    }

    /**
     * 标记处理完成（使用默认TTL）
     *
     * @param key 幂等键
     */
    public void markDone(String key) {
        markDone(key, defaultTtl());
    }

    /**
     * 默认TTL
     * <p>
     * 实现方应保证该值显著大于消息的最大重试窗口，
     * 建议取"最大重试次数×最大单次延迟"的3倍以上。
     * </p>
     *
     * @return 默认占位/完成态有效期
     */
    protected abstract Duration defaultTtl();

    /**
     * 拼装完整幂等键
     * <p>
     * 格式为"作用域:业务键"，作用域通常传队列名。
     * 作用域为空时直接返回业务键。
     * </p>
     *
     * @param scope 作用域（如队列名）
     * @param key   业务幂等键
     * @return 完整幂等键
     */
    protected String composeKey(String scope, String key) {
        if (StrUtil.isEmpty(scope)) {
            return key;
        }
        return scope + ":" + key;
    }

}
