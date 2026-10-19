package com.tbs.mq.v2.core;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.function.SConsumer;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.mq.v2.enums.MqMessageStatusEnum;
import com.tbs.mq.v2.vo.MqMessageVO;
import com.tbs.mq.v2.vo.MqRecordVO;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 消息接收器（v2 记录驱动模型 · 接收段）
 * <p>
 * 承接厂商监听回调的建档与确认职责，从 {@link AbstractMqContext} 拆出：组装消息VO → 批量建档 → 逐条结果按 messageId 配对 → 成功者入内存待处理队列并 ack
 * 结清厂商投递，失败与结果缺席者一律 nak 拒收（宁拒勿漏，每条至多一次，null 元素忽略）。建档失败 nak 兜底是全链路唯一依赖厂商能力的位置， nak 后消息由厂商重投，记录层持续不可用时会反复重投，属预期的背压行为。
 * 组装/校验钩子（genMessageId/assembleMessageVO/getPayload/resolveBusinessId/validateMessageVo）为本类可覆盖方法，由厂商子类实现；
 * recorder 经构造注入，本类只负责接收编排。建档以传入的上下文为作用域，接收器可一对一也可共享给多上下文，由装配决定。
 * </p>
 *
 * @param <R> 厂商原始消息类型（传输层）
 * @param <T> 业务负载类型（消费者面对的类型）
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public abstract class AbstractMqMessageReceiver<R, T extends Serializable> implements IMqMessageReceiver<R, T> {

    /**
     * 消息主记录器，建档落库的执行方
     */
    private final IMqRecorder<R, T> recorder;

    /**
     * 生成消息ID
     * <p>
     * 消息身份由框架自行生成而非取自厂商投递元数据，后续记录、抢占、重试均以该ID为主键。 子类可覆盖以接入自定义发号策略（如雪花ID、业务前缀）。
     * </p>
     *
     * @param message 厂商原始消息
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
     * 会拒收全部消息，属预期保护而非故障。
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
        AssertUtil.isTrueSystemError(messageVo == null).i18n("mq.v2.message.vo.required").throwIt();
        AssertUtil.isTrueSystemError(StrUtil.isEmpty(messageVo.getBusinessId())).i18n("mq.v2.business.id.required")
            .throwIt();
        AssertUtil.isTrueSystemError(messageVo.getRawMessage() == null).i18n("mq.v2.raw.message.required").throwIt();
    }

    /**
     * 构造消息接收器
     *
     * @param recorder 消息主记录器，不能为空
     */
    public AbstractMqMessageReceiver(IMqRecorder<R, T> recorder) {
        this.recorder = recorder;
    }

    /**
     * 接收并建档一批厂商消息
     * <p>
     * 逐条组装校验后批量建档，建档成功项进结果集并回调 onInqueueSuccess，组装异常/建档失败/结果缺席均回调 onInqueueFail。
     * </p>
     *
     * @param context          接收建档归属的消费上下文（队列级作用域）
     * @param messages         厂商原始消息批次，null 元素被忽略
     * @param onInqueueSuccess 建档成功后的回调（一般为厂商 ack），可空
     * @param onInqueueFail    建档失败或结果缺席后的回调（一般为厂商 nak），可空
     * @return 建档成功、可进入处理链路的消息主记录VO列表
     */
    @Override
    public List<MqMessageVO<R, T>> receive(IMqContext<R, T> context, List<R> messages, SConsumer<R> onInqueueSuccess,
        SConsumer<R> onInqueueFail) {
        List<MqMessageVO<R, T>> result = new LinkedList<>();
        if (CollUtil.isEmpty(messages)) {
            log.warn("messages is empty");
            return result;
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
            var recordResult = recorder.record(context, pendingVos);
            var resultMap = new HashMap<String, MqRecordVO<T>>();
            recordResult.stream().filter(record -> record != null && StrUtil.isNotEmpty(record.getMessageId()))
                .forEach(record -> resultMap.put(record.getMessageId(), record));
            for (MqMessageVO<R, T> messageVO : pendingVos) {
                var record = resultMap.get(messageVO.getMessageId());
                if (record == null || !record.isSuccess()) {
                    log.warn("record message failed,messageId={}", messageVO.getMessageId(),
                        record == null ? null : record.getException());
                    nakMsgs.add(messageVO.getRawMessage());
                    continue;
                }
                result.add(messageVO);
                ackMsgs.add(messageVO.getRawMessage());
            }
        }
        if (onInqueueSuccess != null) {
            for (R message : ackMsgs) {
                onInqueueSuccess.accept(message);
            }
        }
        if (onInqueueFail != null) {
            for (R message : nakMsgs) {
                onInqueueFail.accept(message);
            }
        }
        return result;
    }
}
