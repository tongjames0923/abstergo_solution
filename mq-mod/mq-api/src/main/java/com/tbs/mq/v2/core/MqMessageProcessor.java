package com.tbs.mq.v2.core;

import com.tbs.common.util.base.JsonUtils;
import com.tbs.mq.v2.enums.MqMessageStatusEnum;
import com.tbs.mq.v2.vo.MqMessageVO;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 消息处理器（v2 记录驱动模型 · 处理段）
 * <p>
 * 承载独立处理线程的生命周期与主循环，从 {@link AbstractMqContext} 拆出：
 * 取队列消息（带超时阻塞，避免空转）→ 单飞抢占（失败则跳过，不碰他人锁）→
 * 主记录状态推进（PROCESSING → {@code handleMessage} 分发 → PROCESSED，分发环节异常留痕）。
 * 释放占有仅在抢占成功的路径上执行；单条消息的异常不中断循环。
 * 分发业务本身仍归属上下文（子类契约），本类只负责线程调度与状态机看护。一个上下文实例配一个处理器。
 * </p>
 *
 * @param <R> 厂商原始消息类型（传输层）
 * @param <T> 业务负载类型（消费者面对的类型）
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public class MqMessageProcessor<R, T extends Serializable> {

    /**
     * 单次 poll 的超时秒数，无消息时周期性回查运行守卫
     */
    private static final long POLL_TIMEOUT_SECONDS = 90L;

    /**
     * 所属消费上下文，单飞保护内调用其 handleMessage 完成分发
     */
    private final AbstractMqContext<R, T> context;

    /**
     * 消息主记录器，推进主记录状态与失败留痕
     */
    private final IMqRecorder<R, T> recorder;

    /**
     * 消息单飞抢占器
     */
    private final IMqMessageMonopolizer monopolizer;

    /**
     * 内存待处理队列（与上下文的接收段共享）
     */
    private final BlockingQueue<MqMessageVO<R, T>> queue;

    /**
     * 运行守卫，控制处理线程主循环的启停
     */
    private final AtomicBoolean started = new AtomicBoolean(false);

    /**
     * 消息处理线程，{@link #start()} 时创建并启动
     */
    private Thread handlerThread;

    /**
     * 构造消息处理器
     *
     * @param context     所属消费上下文，不能为空
     * @param recorder    消息主记录器，不能为空
     * @param monopolizer 消息单飞抢占器，不能为空
     * @param queue       内存待处理队列，不能为空
     */
    public MqMessageProcessor(AbstractMqContext<R, T> context, IMqRecorder<R, T> recorder,
        IMqMessageMonopolizer monopolizer, BlockingQueue<MqMessageVO<R, T>> queue) {
        this.context = context;
        this.recorder = recorder;
        this.monopolizer = monopolizer;
        this.queue = queue;
    }

    /**
     * 启动处理线程
     */
    public void start() {
        started.set(true);
        handlerThread = new Thread(this::loop);
        handlerThread.start();
    }

    /**
     * 停止处理线程
     * <p>
     * 仅置运行守卫为 false 并中断阻塞的 poll，线程在最迟一个 poll 周期后感知并退出；
     * 内存队列中的残留消息无需转移，其记录已落库，重启后由重试扫描回灌。
     * </p>
     */
    public void stop() {
        started.set(false);
        if (handlerThread != null) {
            handlerThread.interrupt();
        }
    }

    /**
     * 处理线程主循环
     * <p>
     * 单条消息的分发异常不中断循环，异常按主记录留痕后继续取下一条。
     * </p>
     */
    private void loop() {
        log.info("message handler thread started");
        while (started.get()) {
            MqMessageVO<R, T> message = null;
            try {
                message = queue.poll(POLL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("InterruptedException", e);
            }
            if (message == null) {
                continue;
            }
            boolean monopolized = false;
            try {
                String msgText = JsonUtils.toJSONString(message);
                monopolized = monopolizer.monopolize(message);
                if (!monopolized) {
                    log.info("monopolized failed message: {}", msgText);
                    continue;
                }
                log.info("poll message: {}", msgText);
                recorder.updateFlag(message.getMessageId(), MqMessageStatusEnum.PROCESSING);
                context.handleMessage(message);
                recorder.updateFlag(message.getMessageId(), MqMessageStatusEnum.PROCESSED);
            } catch (Exception e) {
                log.warn("handle message {} error", message.getMessageId());
                log.error("error", e);
                recorder.errorFlag(message.getMessageId(), e);
            } finally {
                if (monopolized) {
                    monopolizer.release(message);
                }
            }
        }
        log.info("message handler thread stopped");
    }
}
