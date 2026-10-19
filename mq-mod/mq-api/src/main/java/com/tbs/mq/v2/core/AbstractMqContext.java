package com.tbs.mq.v2.core;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.mq.v2.consume.IConsumerRegistry;
import com.tbs.mq.v2.consume.IMqConsumeRecorder;
import com.tbs.mq.v2.consume.IMqConsumer;
import com.tbs.mq.v2.enums.MqMessageConsumeStatusEnum;
import com.tbs.mq.v2.exception.MqConsumeException;
import com.tbs.mq.v2.vo.MqConsumeVO;
import com.tbs.mq.v2.vo.MqMessageVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * MQ消费上下文抽象基类（v2 记录驱动模型）
 * <p>
 * 厂商无关的消费骨架：厂商通道（RabbitMQ/其他）退化为纯传输层（类型 R），业务负载（类型 T）
 * 由子类经 {@link #getPayload(Object)} 转换，可靠性由记录层承担。全链路遵循固定流程：
 * <ol>
 *     <li>接收：{@link #messageReceived} 按批次经 {@link IMqRecorder} 落库建立主记录，逐条结果按 messageId 配对：
 *     成功者进内存待处理队列并 {@link #ackMessage} 结清厂商投递；失败与结果缺席者 {@link #nakMessage} 拒收（每条至多一次），
 *     由厂商重投兜底——这是全链路唯一依赖厂商能力的位置；</li>
 *     <li>处理：独立处理线程按序取队列消息，经 {@link IMqMessageMonopolizer} 单飞抢占后推进主记录状态机
 *     RECEIVED → PROCESSING → PROCESSED/FAILED；{@link #handleMessage} 对当前负载类型注册的全部消费者广播执行，
 *     每个消费者的成败与耗时经 {@link IMqConsumeRecorder} 写入消费台账——单消费者失败只留 FAIL 台账，
 *     不中断其他消费者，也不改变主记录终态（PROCESSED 指分发完成，不等于全员成功）；</li>
 *     <li>重试：完全基于记录层（主记录状态 + 消费台账状态 + 错误信息）由外部扫描组件回灌，
 *     不依赖厂商死信/重投能力。</li>
 * </ol>
 * 子类职责：实现 {@link #getPayload}/{@link #resolveBusinessId}/{@link #getMessageType}/{@link #getMessagePayloadType}
 * 与 ackMessage/nakMessage 对接具体厂商的确认语义，并在厂商监听回调中将原始消息交给 {@link #messageReceived}；
 * 业务消费由注册到 {@code IConsumerRegistry} 的 {@code IMqConsumer} 承担。一个上下文实例对应一个队列/一个环境，
 * 多队列扩容通过注册多个上下文实例实现。
 * </p>
 * <p>
 * 实现拆分：接收段（组装/建档/配对/确认）由 {@link AbstractMqMessageReceiver} 编排，处理线程生命周期由
 * {@link MqMessageProcessor} 看护，本类作为装配门面保留钩子契约与消费台账分发（{@link #handleMessage}）。
 * </p>
 *
 * @param <R> 厂商原始消息类型（传输层）
 * @param <T> 业务负载类型（消费者面对的类型）
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public abstract class AbstractMqContext<R, T extends Serializable>
    implements IMqContext<R, T>, InitializingBean, DisposableBean {

    /**
     * 内存待处理队列，接收段建档成功消息的去处与处理线程的来源，两端共享；进程重启后队列清空，未处理消息由记录层扫描回灌
     */
    private BlockingQueue<MqMessageVO<R, T>> messageQueue = new LinkedBlockingQueue<>();

    /**
     * 消费台账记录器，逐消费者记录每次分发的成败与耗时
     */
    private IMqConsumeRecorder<T> consumeRecorder;

    /**
     * 消息接收器，接收段（组装/建档/配对/确认）编排协作类
     */
    private final IMqMessageReceiver<R, T> receiver;

    /**
     * 消息处理器，处理线程生命周期与主记录状态机看护协作类
     */
    private final MqMessageProcessor<R, T> processor;

    @Resource
    private IConsumerRegistry consumerRegistry;


    /**
     * 构造消费上下文
     * <p>
     * 主记录器、台账记录器与抢占器是可靠性的根基，缺失时上下文无法保证不丢消息与台账可查，故在装配期即拒绝空值。
     * </p>
     *
     * @param recorder             消息主记录器，不能为空
     * @param mqMessageMonopolizer 消息单飞抢占器，不能为空
     * @param consumeRecorder      消费台账记录器，不能为空
     */
    public AbstractMqContext(IMqRecorder<R, T> recorder, IMqMessageReceiver<R, T> messageReceiver,
        IMqMessageMonopolizer mqMessageMonopolizer,
        IMqConsumeRecorder<T> consumeRecorder) {

        AssertUtil.isTrueSystemError(mqMessageMonopolizer == null).message("mqMessageMonopolizer cannot be null")
            .throwIt();

        AssertUtil.isTrueSystemError(recorder == null).message("recorder cannot be null").throwIt();

        AssertUtil.isTrueSystemError(consumeRecorder == null).message("consumeRecorder cannot be null").throwIt();

        this.receiver = messageReceiver;
        this.consumeRecorder = consumeRecorder;
        this.processor = new MqMessageProcessor<>(this, recorder, mqMessageMonopolizer, messageQueue);

    }



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
        messageQueue.addAll(receiver.receive(messages, this::ackMessage, this::nakMessage));
    }

    /**
     * 业务消费分发
     * <p>
     * 由处理线程在单飞保护与 PROCESSING 状态内调用：当前负载类型未注册任何消费者时抛可重试
     * {@code MqConsumeException}（主记录置 FAILED，等待消费者注册后由记录层回灌）；否则对全部注册消费者
     * 逐个执行，每个消费者的成败与耗时汇总为消费台账一次性落库——单消费者失败只留 FAIL 台账，
     * 不中断其他消费者，也不改变主记录终态（PROCESSED 指分发完成，不等于全员成功）。
     * 本方法内不做任何厂商侧确认动作。
     * </p>
     *
     * @param messageVo 消息主记录VO，为 null 时直接忽略
     */
    @Override
    public void handleMessage(MqMessageVO<R, T> messageVo) {
        if (messageVo == null) {
            log.warn("message vo is null");
            return;
        }
        log.debug("message vo: {}", messageVo);
        List<IMqConsumer<T>> consumers = consumerRegistry.getConsumers(getMessagePayloadType());
        AssertUtil.isTrueSystemError(CollUtil.isEmpty(consumers)).as(e -> {
            return MqConsumeException.retryable(e, "no consumer registered for message type: {}",
                getMessagePayloadType());
        }).throwIt();
        List<MqConsumeVO<T>> consumeVos = new LinkedList<>();
        for (IMqConsumer<T> consumer : consumers) {
            Date startTime = new Date();
            try {
                consumer.consume(messageVo.getPayload());
                consumeVos.add(
                    MqConsumeVO.<T>builder().messageId(messageVo.getMessageId()).businessId(messageVo.getBusinessId())
                        .consumeId(consumer.consumeId()).consumeStartTime(startTime).consumeEndTime(new Date())
                        .flag(MqMessageConsumeStatusEnum.SUCCESS.getCode()).build());
            } catch (Exception e) {
                log.warn("consumer {} consume error", consumer.getClass().getName(), e);
                consumeVos.add(
                    MqConsumeVO.<T>builder().messageId(messageVo.getMessageId()).consumeId(consumer.consumeId())
                        .consumeStartTime(startTime).consumeEndTime(new Date()).businessId(messageVo.getBusinessId())
                        .flag(MqMessageConsumeStatusEnum.FAIL.getCode()).exception(e).build());
            }
        }
        consumeRecorder.record(consumeVos);

    }

    /**
     * 销毁上下文：停止处理线程
     * <p>
     * 委托 {@link MqMessageProcessor#stop()}，退出语义详见其注释。
     * </p>
     *
     * @throws Exception 关闭异常时记录日志，不阻断其他bean销毁
     */
    @Override
    public void destroy() throws Exception {
        processor.stop();
    }

    /**
     * 初始化上下文：启动消息处理线程
     * <p>
     * 委托 {@link MqMessageProcessor#start()}，主循环三段语义详见 {@link MqMessageProcessor} 类注释。
     * </p>
     *
     * @throws Exception 初始化失败时抛出，阻断bean启动
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        processor.start();
    }
}
