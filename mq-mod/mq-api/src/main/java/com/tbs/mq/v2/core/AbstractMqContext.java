package com.tbs.mq.v2.core;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.JsonUtils;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.mq.v2.consume.IConsumerRegistry;
import com.tbs.mq.v2.consume.IMqConsumeRecorder;
import com.tbs.mq.v2.consume.IMqConsumer;
import com.tbs.mq.v2.enums.MqMessageConsumeStatusEnum;
import com.tbs.mq.v2.enums.MqMessageStatusEnum;
import com.tbs.mq.v2.exception.MqConsumeException;
import com.tbs.mq.v2.vo.MqConsumeVO;
import com.tbs.mq.v2.vo.MqMessageVO;
import com.tbs.mq.v2.vo.MqRecordVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

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
     * 运行守卫，控制处理线程主循环的启停
     */
    private AtomicBoolean started = new AtomicBoolean(false);

    /**
     * 内存待处理队列，承接已落库建档与处理线程之间的缓冲；进程重启后队列清空，未处理消息由记录层扫描回灌
     */
    private BlockingQueue<MqMessageVO<R, T>> messageQueue = new LinkedBlockingQueue<>();

    /**
     * 消息处理线程，{@link #afterPropertiesSet} 时创建并启动
     */
    private Thread messageHandlerThread;

    /**
     * 消息单飞抢占器，保证同一消息同一时刻仅被一个处理者分发
     */
    private IMqMessageMonopolizer mqMessageMonopolizer;

    /**
     * 消息主记录器，承载消费链路的可靠性真源
     */
    private IMqRecorder<R, T> recorder;

    /**
     * 消费台账记录器，逐消费者记录每次分发的成败与耗时
     */
    private IMqConsumeRecorder<T> consumeRecorder;

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
    public AbstractMqContext(IMqRecorder<R, T> recorder, IMqMessageMonopolizer mqMessageMonopolizer,
        IMqConsumeRecorder<T> consumeRecorder) {

        AssertUtil.isTrueSystemError(mqMessageMonopolizer == null).message("mqMessageMonopolizer cannot be null")
            .throwIt();

        AssertUtil.isTrueSystemError(recorder == null).message("recorder cannot be null").throwIt();

        AssertUtil.isTrueSystemError(consumeRecorder == null).message("consumeRecorder cannot be null").throwIt();

        this.recorder = recorder;
        this.mqMessageMonopolizer = mqMessageMonopolizer;
        this.consumeRecorder = consumeRecorder;


    }

    /**
     * 生成消息ID
     * <p>
     * 消息身份由框架自行生成而非取自厂商投递元数据，后续记录、抢占、重试均以该ID为主键。 子类可覆盖以接入自定义发号策略（如雪花ID、业务前缀）。
     * </p>
     *
     * @return 全局唯一的消息ID，不为空
     */
    protected String genMessageId(R message) {
        return UuidUtil.getInstance().getUuidString();
    }

    /**
     * 组装消息VO
     * <p>
     * 构建待落库的初始主记录：框架生成的消息ID + 业务标识 + 厂商原始消息 + 转换后的业务负载 + RECEIVED 初始状态。
     * </p>
     *
     * @param message 厂商原始消息
     * @return 初始状态的消息主记录VO，不为空
     */
    protected MqMessageVO<R, T> assembleMessageVO(R message) {
        return MqMessageVO.<R, T>builder().messageId(genMessageId(message)).businessId(resolveBusinessId(message))
            .flag(MqMessageStatusEnum.RECEIVED.getCode()).rawMessage(message).payload(getPayload(message)).build();
    }

    /**
     * 转换业务负载
     * <p>
     * 传输层与业务层的边界：把厂商消息（R）反序列化/解析为消费者直接面对的业务对象（T）。 消费链路只传递负载，原始消息仅保留在主记录中供排查与重放。
     * </p>
     *
     * @param e 厂商原始消息
     * @return 业务负载
     */
    protected abstract T getPayload(R e);

    /**
     * 解析业务标识
     * <p>
     * 业务标识是建档必填项（幂等去重与人工排查的锚点），基类无法从泛型原始消息中得知，由子类提供。 上下文绑定 {@code IMqConsumer} 的场景直接返回其 businessId() 即可；未提供时
     * {@link #messageReceived} 会拒收全部消息，属预期保护而非故障。
     * </p>
     *
     * @param message 厂商原始消息
     * @return 业务标识，不为空
     */
    protected abstract String resolveBusinessId(R message);

    /**
     * 校验消息VO
     * <p>
     * 建档前的最低完整性要求：VO、业务标识、原始消息缺一不可。业务标识为空通常意味着子类未覆盖 {@link #resolveBusinessId}。
     * </p>
     *
     * @param messageVo 待校验的消息VO
     */
    protected void validateMessageVo(MqMessageVO<R, T> messageVo) {
        AssertUtil.isTrueSystemError(messageVo == null).message("messageVo cannot be null").throwIt();
        AssertUtil.isTrueSystemError(StrUtil.isEmpty(messageVo.getBusinessId()))
            .message("businessId cannot be null, override resolveBusinessId in subclass").throwIt();
        AssertUtil.isTrueSystemError(messageVo.getRawMessage() == null).message("rawMessage cannot be null").throwIt();
    }

    /**
     * 消息接收处理
     * <p>
     * 厂商监听回调的入口，在厂商线程上执行，先建档后确认：组装失败的条目不进入建档； 建档结果按 messageId 与入参配对，成功者入内存待处理队列并 ack 结清厂商投递， 失败与结果缺席者一律
     * nak（宁拒勿漏，每条至多一次，null 元素忽略）。 消息绝不允许在未建档状态下进入处理流程（否则重试、留痕、恢复全部不可达）。 nak 后消息由厂商重投，记录层持续不可用时会反复重投，属预期的背压行为。
     * </p>
     *
     * @param messages 厂商原始消息批次，可含 null 元素（被忽略）
     */
    @Override
    public void messageReceived(List<R> messages) {
        if (CollUtil.isEmpty(messages)) {
            log.warn("messages is empty");
            return;
        }
        var pendingVos = new LinkedList<MqMessageVO<R, T>>();
        var ackMsgs = new LinkedList<R>();
        var nakMsgs = new LinkedList<R>();
        for (R message : messages) {
            if (message == null) {
                log.warn("ignore null message in batch");
                continue;
            }
            try {
                var messageVO = assembleMessageVO(message);
                validateMessageVo(messageVO);
                pendingVos.add(messageVO);
            } catch (Exception e) {
                log.error("assemble message vo error", e);
                nakMsgs.add(message);
            }
        }
        if (!CollUtil.isEmpty(pendingVos)) {
            var recordResult = recorder.record(pendingVos);
            var resultMap = new HashMap<String, MqRecordVO<T>>();
            if (!CollUtil.isEmpty(recordResult)) {
                for (MqRecordVO<T> record : recordResult) {
                    if (record != null && StrUtil.isNotEmpty(record.getMessageId())) {
                        resultMap.put(record.getMessageId(), record);
                    }
                }
            }
            for (MqMessageVO<R, T> messageVO : pendingVos) {
                var record = resultMap.get(messageVO.getMessageId());
                if (record == null || !record.isSuccess()) {
                    log.warn("record message failed,messageId={}", messageVO.getMessageId(),
                        record == null ? null : record.getException());
                    nakMsgs.add(messageVO.getRawMessage());
                    continue;
                }
                messageQueue.add(messageVO);
                ackMsgs.add(messageVO.getRawMessage());
            }
        }
        for (R message : ackMsgs) {
            ackMessage(message);
        }
        for (R message : nakMsgs) {
            nakMessage(message);
        }
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
     * 仅置运行守卫为 false，处理线程在最迟一个 poll 周期（90秒）后感知并退出； 内存队列中的残留消息无需转移，其记录已落库，重启后由重试扫描回灌。
     * </p>
     *
     * @throws Exception 关闭异常时记录日志，不阻断其他bean销毁
     */
    @Override
    public void destroy() throws Exception {
        started.set(false);
        messageHandlerThread.interrupt();
    }

    /**
     * 初始化上下文：启动消息处理线程
     * <p>
     * 处理线程主循环分三段： 取队列消息（带超时阻塞，避免空转）→ 单飞抢占（失败则跳过，不碰他人锁）→
     * 主记录状态推进（PROCESSING → handleMessage 分发 → PROCESSED，分发环节异常留痕并置 FAILED）。
     * 释放占有仅在抢占成功的路径上执行；单条消息的异常不中断循环。
     * </p>
     *
     * @throws Exception 初始化失败时抛出，阻断bean启动
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        started.set(true);
        messageHandlerThread = new Thread(() -> {
            log.info("message handler thread started");
            while (started.get()) {
                MqMessageVO<R, T> message = null;
                try {
                    message = messageQueue.poll(90, java.util.concurrent.TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    log.error("InterruptedException", e);
                }
                if (message == null) {
                    continue;
                }
                boolean monopolized = false;
                try {
                    String msgText = JsonUtils.toJSONString(message);
                    monopolized = mqMessageMonopolizer.monopolize(message);
                    if (!monopolized) {
                        log.info("monopolized failed message: {}", msgText);
                        continue;
                    }
                    log.info("poll message: {}", msgText);
                    recorder.updateFlag(message.getMessageId(), MqMessageStatusEnum.PROCESSING);
                    handleMessage(message);
                    recorder.updateFlag(message.getMessageId(), MqMessageStatusEnum.PROCESSED);
                } catch (Exception e) {
                    log.warn("handle message {} error", message.getMessageId());
                    log.error("error", e);
                    recorder.errorFlag(message.getMessageId(), e);
                    recorder.updateFlag(message.getMessageId(), MqMessageStatusEnum.FAILED);
                    continue;
                } finally {
                    if (monopolized) {
                        mqMessageMonopolizer.release(message);
                    }
                }

            }
            log.info("message handler thread stopped");

        });
        messageHandlerThread.start();

    }
}
