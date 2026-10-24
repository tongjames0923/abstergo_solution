package com.tbs.redis.mq;

import com.tbs.common.util.base.StrUtil;
import com.tbs.lock.ILock;
import com.tbs.lock.ILockProvider;
import com.tbs.mq.v2.core.IMqContext;
import com.tbs.mq.v2.core.IMqMessageMonopolizer;
import com.tbs.mq.v2.vo.MqMessageIncomeVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.Duration;

/**
 * 消息级单飞抢占器的分布式锁实现（v2 记录驱动模型 · 集群单飞）
 * <p>
 * 以"上下文ID + messageId"为锁键，经 {@link ILockProvider} 取分布式锁实现跨实例单飞：
 * {@link #monopolize} 仅在锁首次被当前线程持有时返回 {@code true}，其他实例/线程持有期间返回 {@code false}；
 * {@link #release} 仅由持有者释放，他人占有不触碰。锁的具体实现由宿主的 {@code tbs.lock.lock-provider-type}
 * 决定（本模块配套提供 {@code RedissonLockProvider}），故本类不绑定 Redisson 客户端。
 * </p>
 * <p>
 * 与内存实现的差异：占有关系落在分布式锁上，进程重启或跨实例部署后仍然有效；锁按线程持有，
 * {@code monopolize}/{@code release} 必须在同一处理线程内配对调用（{@code AbstractMqContext} 的处理循环已保证）。
 * 锁的生命周期由 provider 决定：RedissonLock 采用看门狗续期（默认 30 秒续一次），进程崩溃后自动释放，
 * 但处理耗时不受限；若宿主 provider 采用固定租约，则租约需大于单条消息的最长处理时间。
 * </p>
 *
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public class RedisMqMessageMonopolizer implements IMqMessageMonopolizer {

    /**
     * 锁键前缀，冒号分层：mq:monopoly:{contextId}:{messageId}
     */
    private static final String KEY_PREFIX = "mq:monopoly:";

    /**
     * 分布式锁提供者，由宿主 {@code tbs.lock.lock-provider-type} 装配
     */
    @Resource
    private ILockProvider lockProvider;

    /**
     * 抢占消息：同一上下文下的同一 messageId 同一时刻仅允许一个持有者
     * <p>
     * 不等待（零超时），抢占失败立即返回 false 由调用方跳过，符合分发段"抢不到就不处理"的语义。
     * </p>
     *
     * @param context 当前上下文（队列级作用域）
     * @param message 待抢占的消息主记录VO
     * @param <R>     厂商原始消息类型
     * @param <T>     业务负载类型
     * @return true 表示抢占成功，调用方负责处理完成后 release；false 表示他人持有或入参非法
     */
    @Override
    public <R, T extends Serializable> boolean monopolize(IMqContext<R, T> context, MqMessageIncomeVO<R, T> message) {
        if (message == null || StrUtil.isEmpty(message.getMessageId())) {
            log.debug("[MQ-v2] redis monopolize reject, blank message/messageId, contextId={}", contextId(context));
            return false;
        }
        boolean acquired = lock(context, message.getMessageId()).tryLock(Duration.ZERO);
        log.debug("[MQ-v2] redis monopolize {}, key={}", acquired ? "success" : "failed(held by others)",
            key(context, message.getMessageId()));
        return acquired;
    }

    /**
     * 释放消息的占有，仅限抢占成功的那个持有者调用
     * <p>
     * 先校验当前线程是否持有再解锁：锁已过期或已被他人持有时跳过，不误释放他人占有。
     * </p>
     *
     * @param context 当前上下文（队列级作用域）
     * @param message 已分发完成的消息主记录VO
     * @param <R>     厂商原始消息类型
     * @param <T>     业务负载类型
     * @return 释放后的消息主记录VO（原样返回）
     */
    @Override
    public <R, T extends Serializable> MqMessageIncomeVO<R, T> release(IMqContext<R, T> context,
        MqMessageIncomeVO<R, T> message) {
        if (message == null || StrUtil.isEmpty(message.getMessageId())) {
            log.debug("[MQ-v2] redis release skip, blank message/messageId, contextId={}", contextId(context));
            return message;
        }
        ILock lock = lock(context, message.getMessageId());
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unLock();
                log.debug("[MQ-v2] redis release done, key={}", key(context, message.getMessageId()));
            } else {
                log.warn("[MQ-v2] redis release skipped, not held by current thread, key={}",
                    key(context, message.getMessageId()));
            }
        } catch (IllegalMonitorStateException e) {
            log.warn("[MQ-v2] redis release raced with lock expiry, key={}", key(context, message.getMessageId()));
        }
        return message;
    }

    /**
     * 取分布式锁实例
     *
     * @param context   当前上下文
     * @param messageId 消息ID
     * @return 分布式锁
     */
    private ILock lock(IMqContext<?, ?> context, String messageId) {
        return lockProvider.getLocker(key(context, messageId));
    }

    /**
     * 构造锁键
     *
     * @param context   当前上下文
     * @param messageId 消息ID
     * @return 锁键
     */
    private static String key(IMqContext<?, ?> context, String messageId) {
        return KEY_PREFIX + contextId(context) + ":" + messageId;
    }

    /**
     * 取上下文ID，空上下文归入 0 作用域，与记录层保持同一隔离口径
     *
     * @param context 当前上下文
     * @return 上下文ID
     */
    private static Long contextId(IMqContext<?, ?> context) {
        Long id = context == null ? null : context.getContextId();
        return id == null ? 0L : id;
    }
}
