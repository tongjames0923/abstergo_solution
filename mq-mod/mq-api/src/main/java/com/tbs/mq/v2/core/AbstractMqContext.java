package com.tbs.mq.v2.core;

import com.tbs.mq.v2.enums.MqMessageStatusEnum;
import com.tbs.mq.v2.vo.MqMessageVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactoryAware;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MQ消费上下文抽象基类（v2 记录驱动模型）
 * <p>
 * 厂商无关的消费骨架：厂商通道（RabbitMQ/其他）退化为纯传输层（类型 R），业务负载（类型 T）
 * 由接收段经 {@code getPayload} 转换，可靠性由记录层（{@link IMqRecorder}）承担。全链路遵循固定流程：
 * <ol>
 *     <li>接收：{@link #messageReceived} 委托 {@link IMqMessageReceiver} 按批次建档，逐条结果按 messageId 配对：
 *     成功者进内存待处理队列并 {@link #ackMessage} 结清厂商投递；失败与结果缺席者 {@link #nakMessage} 拒收（每条至多一次），
 *     由厂商重投兜底——这是全链路唯一依赖厂商能力的位置；</li>
 *     <li>处理：独立处理线程按序取队列消息，经 {@link IMqMessageMonopolizer} 单飞抢占后，在记录层事务内
 *     推进主状态机 RECEIVED → PROCESSING → PROCESSED/FAILED；{@link IMqMessageProcessor#process} 对当前负载类型
 *     注册的全部消费者广播执行，每个消费者的成败与耗时经 {@link IMqRecorder#recordConsume} 写入消费台账——
 *     单消费者失败只留 FAIL 台账，不中断其他消费者，也不改变主记录终态（PROCESSED 指分发完成，不等于全员成功）；</li>
 *     <li>重试：完全基于记录层（主记录状态 + 消费台账状态 + 错误信息）由外部扫描组件回灌，
 *     不依赖厂商死信/重投能力。</li>
 * </ol>
 * 子类职责：实现 {@link #getMessageType}/{@link #getMessagePayloadType}
 * 与 ackMessage/nakMessage 对接具体厂商的确认语义，并在厂商监听回调中将原始消息交给 {@link #messageReceived}；
 * 业务消费由注册到 {@code IConsumerRegistry} 的 {@code IMqConsumer} 承担。一个上下文实例对应一个队列/一个环境，
 * 多队列扩容通过注册多个上下文实例实现。
 * </p>
 * <p>
 * 实现拆分：接收段（组装/建档/配对/确认）由 {@link AbstractMqMessageReceiver} 编排，
 * 分发段（广播消费/构建台账）由 {@link AbstractMqMessageProcessor} 承担，
 * 本类作为装配门面保留待处理队列、处理线程生命周期与厂商确认钩子。
 * 门面以自身 {@code this} 作为上下文作用域传入接收/记录/抢占层（见 {@link IMqContext#getContextId()}），
 * 使共享的记录器/抢占器实例能按队列隔离归属；业务消费者不感知上下文，仅接收负载。
 * </p>
 *
 * @param <R> 厂商原始消息类型（传输层）
 * @param <T> 业务负载类型（消费者面对的类型）
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public abstract class AbstractMqContext<R, T extends Serializable> implements IMqContext<R, T> {

    /**
     * 单次 poll 的超时秒数，无消息时周期性回查运行守卫
     */
    private static final long POLL_TIMEOUT_SECONDS = 90L;
    /**
     * 运行守卫，控制处理线程主循环的启停
     */
    private final AtomicBoolean started = new AtomicBoolean(false);
    /**
     * 消息处理线程，{@link #afterPropertiesSet()} 时创建并启动
     */
    private Thread handlerThread;
    /**
     * 内存待处理队列，接收段建档成功消息的去处与处理线程的来源，两端共享；进程重启后队列清空，未处理消息由记录层扫描回灌
     */
    private BlockingQueue<MqMessageVO<R, T>> messageQueue = new LinkedBlockingQueue<>();

    /**
     * 消息接收器，接收段（组装/建档/配对/确认）编排协作类
     */
    @Resource
    private IMqMessageReceiver<R, T> receiver;

    /**
     * 消息级单飞抢占器，保护同一 messageId 不被并行重处理
     */
    @Resource
    private IMqMessageMonopolizer mqMessageMonopolizer;

    /**
     * 消息记录器，推进主记录状态、落库消费台账与失败留痕
     */
    @Resource
    private IMqRecorder<R, T> recorder;

    /**
     * 消息分发器，分发段（广播消费/构建台账）协作类
     */
    @Resource
    private IMqMessageProcessor<R, T> processor;

    /**
     * 消息接收处理
     * <p>
     * 厂商监听回调的入口，委托 {@link IMqMessageReceiver#receive} 完成建档与确认，语义详见其类注释。
     * </p>
     *
     * @param messages 厂商原始消息批次，可含 null 元素（被忽略）
     */
    @Override
    public void messageReceived(List<R> messages) {
        int rawSize = messages == null ? 0 : messages.size();
        log.debug("[MQ-v2] receive batch start, contextId={}, rawSize={}", getContextId(), rawSize);
        List<MqMessageVO<R, T>> inqueued = receiver.receive(this, messages, this::ackMessage, this::nakMessage);
        messageQueue.addAll(inqueued);
        log.debug("[MQ-v2] receive batch done, contextId={}, rawSize={}, inqueueSize={}, queueSize={}", getContextId(),
            rawSize, inqueued.size(), messageQueue.size());
    }

    /**
     * 获取当前内存待处理队列中待消费消息数量
     * <p>
     * 队列归属基类，故由本类直接实现，子类无需也不应覆盖。
     * </p>
     *
     * @return 待处理消息数量
     */
    @Override
    public Integer getQueueSize() {
        return messageQueue.size();
    }

    /**
     * Invoked by the containing {@code BeanFactory} on destruction of a bean.
     *
     * @throws Exception in case of shutdown errors. Exceptions will get logged but not rethrown to allow other beans to
     *                   release their resources as well.
     */
    @Override
    public void destroy() throws Exception {
        log.info("[MQ-v2] context destroying, stopping handler thread, contextId={}, queueSize={}", getContextId(),
            messageQueue.size());
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
        Long contextId = getContextId();
        log.info("[MQ-v2] message handler thread started, contextId={}", contextId);
        while (started.get()) {
            MqMessageVO<R, T> message = null;
            try {
                message = messageQueue.poll(POLL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("[MQ-v2] handler thread interrupted while polling, contextId={}", contextId, e);
            }
            if (message == null) {
                continue;
            }
            log.debug("[MQ-v2] poll message, contextId={}, messageId={}, queueSize={}", contextId,
                message.getMessageId(), messageQueue.size());
            boolean monopolized = false;
            Integer transactionId = null;
            try {
                monopolized = mqMessageMonopolizer.monopolize(this, message);
                if (!monopolized) {
                    log.debug("[MQ-v2] monopolize failed (held by others), skip, contextId={}, messageId={}",
                        contextId, message.getMessageId());
                    continue;
                }
                log.debug("[MQ-v2] monopolize success, contextId={}, messageId={}", contextId, message.getMessageId());
                transactionId = recorder.beginTransaction(this);
                log.debug("[MQ-v2] begin transaction, contextId={}, messageId={}, transactionId={}", contextId,
                    message.getMessageId(), transactionId);
                recorder.updateFlag(this, message.getMessageId(), MqMessageStatusEnum.PROCESSING);
                log.debug("[MQ-v2] update flag PROCESSING, contextId={}, messageId={}", contextId,
                    message.getMessageId());
                var consumeResult = processor.process(this, message);
                log.debug("[MQ-v2] dispatched message, contextId={}, messageId={}, consumeRows={}", contextId,
                    message.getMessageId(), consumeResult == null ? 0 : consumeResult.size());
                int ledgerRows = recorder.recordConsume(this, consumeResult);
                log.debug("[MQ-v2] record consume ledger, contextId={}, messageId={}, persistedRows={}", contextId,
                    message.getMessageId(), ledgerRows);
                recorder.updateFlag(this, message.getMessageId(), MqMessageStatusEnum.PROCESSED);
                log.debug("[MQ-v2] update flag PROCESSED, contextId={}, messageId={}", contextId,
                    message.getMessageId());
            } catch (Exception e) {
                log.warn("[MQ-v2] handle message error, mark FAILED, contextId={}, messageId={}", contextId,
                    message.getMessageId(), e);
                recorder.errorFlag(this, message.getMessageId(), e);
            } finally {
                if (monopolized) {
                    mqMessageMonopolizer.release(this, message);
                    log.debug("[MQ-v2] release monopoly, contextId={}, messageId={}", contextId, message.getMessageId());
                }
                if (transactionId != null) {
                    recorder.endTransaction(this, transactionId);
                    log.debug("[MQ-v2] end transaction, contextId={}, messageId={}, transactionId={}", contextId,
                        message.getMessageId(), transactionId);
                }
            }
        }
        log.info("[MQ-v2] message handler thread stopped, contextId={}", contextId);
    }

    /**
     * Invoked by the containing {@code BeanFactory} after it has set all bean properties and satisfied
     * {@link BeanFactoryAware}, {@code ApplicationContextAware} etc.
     * <p>This method allows the bean instance to perform validation of its overall
     * configuration and final initialization when all bean properties have been set.
     *
     * @throws Exception in the event of misconfiguration (such as failure to set an essential property) or if
     *                   initialization fails for any other reason
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        started.set(true);
        handlerThread = new Thread(this::loop);
        handlerThread.start();
        log.info("[MQ-v2] context initialized, handler thread launched, contextId={}, payloadType={}", getContextId(),
            getMessagePayloadType().getName());
    }
}
