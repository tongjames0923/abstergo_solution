package com.tbs.mq.v2.core;

import com.tbs.common.context.IOperatorContext;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.mq.constants.MqHeaderConstants;
import com.tbs.mq.v2.consume.IConsumerRegistry;
import com.tbs.mq.v2.consume.IMqConsumer;
import com.tbs.mq.v2.enums.MqMessageConsumeStatusEnum;
import com.tbs.mq.v2.exception.MqConsumeException;
import com.tbs.mq.v2.vo.MqConsumeVO;
import com.tbs.mq.v2.vo.MqMessageIncomeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 消息分发处理器抽象基类（v2 记录驱动模型 · 分发段）
 * <p>
 * 从 {@link AbstractMqContext} 拆出的分发职责：按上下文负载类型从 {@link IConsumerRegistry} 回查消费者集合，
 * 对每个注册消费者广播执行 {@code consume(payload)} 并各自计时、捕获异常，逐条构建 SUCCESS/FAIL 台账行返回；
 * 单消费者异常不中断其余消费者。消息头携带 {@link MqHeaderConstants#OPERATOR_ID} 时先还原发起人再消费，
 * 使消费链路内的审计字段与发起端一致；无注册消费者时抛可重试 {@link MqConsumeException}，
 * 由上下文将主记录置 FAILED 等待注册后回灌。子类只需提供 {@link #getConsumerRegistry()}。
 * </p>
 *
 * @param <R> 厂商原始消息类型（传输层）
 * @param <T> 业务负载类型（消费者面对的类型）
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public abstract class AbstractMqMessageProcessor<R, T extends Serializable> implements IMqMessageProcessor<R, T> {

    /**
     * 当前操作人上下文，由业务模块装配；缺失时不还原发起人
     */
    @Autowired(required = false)
    private IOperatorContext operatorContext;

    /**
     * 提供消费者路由表，分发时按负载类型回查消费者集合
     *
     * @return 消费者注册器，不为空
     */
    protected abstract IConsumerRegistry getConsumerRegistry();

    /**
     * 构建一条消费成功台账行
     *
     * @param startTime 消费开始时间
     * @param context   当前上下文
     * @param consumer  命中的消费者
     * @param messageVo 消息主记录VO
     * @return flag=SUCCESS 的台账行
     */
    protected MqConsumeVO<T> buildConsumeSuccess(Date startTime, IMqContext<R, T> context, IMqConsumer<T> consumer,
        MqMessageIncomeVO<R, T> messageVo) {
        return MqConsumeVO.<T>builder().messageId(messageVo.getMessageId()).businessId(messageVo.getBusinessId())
            .consumeId(consumer.consumeId()).consumeStartTime(startTime).consumeEndTime(new Date())
            .flag(MqMessageConsumeStatusEnum.SUCCESS.getCode()).build();
    }

    /**
     * 构建一条消费失败台账行，携带失败原因异常供后续重试扫描按 retryFlag 判定
     *
     * @param startTime 消费开始时间
     * @param context   当前上下文
     * @param consumer  命中的消费者
     * @param messageVo 消息主记录VO
     * @param e         消费失败异常
     * @return flag=FAIL 且携带 exception 的台账行
     */
    protected MqConsumeVO<T> buildConsumeFail(Date startTime, IMqContext<R, T> context, IMqConsumer<T> consumer,
        MqMessageIncomeVO<R, T> messageVo, Exception e) {
        return MqConsumeVO.<T>builder().messageId(messageVo.getMessageId()).businessId(messageVo.getBusinessId())
            .consumeId(consumer.consumeId()).consumeStartTime(startTime).consumeEndTime(new Date())
            .flag(MqMessageConsumeStatusEnum.FAIL.getCode()).exception(e).build();
    }

    @Override
    public List<MqConsumeVO<T>> process(IMqContext<R, T> context, MqMessageIncomeVO<R, T> messageVo) {
        Long contextId = context == null ? null : context.getContextId();
        if (messageVo == null) {
            log.warn("[MQ-v2] process skip, message vo is null, contextId={}", contextId);
            return CollUtil.emptyList();
        }
        log.debug("[MQ-v2] process start, contextId={}, messageId={}, businessId={}, payloadType={}", contextId,
            messageVo.getMessageId(), messageVo.getBusinessId(), context.getMessagePayloadType().getName());
        List<IMqConsumer<T>> consumers = getConsumerRegistry().getConsumers(context.getMessagePayloadType());
        log.debug("[MQ-v2] resolved consumers, contextId={}, messageId={}, count={}", contextId,
            messageVo.getMessageId(), consumers == null ? 0 : consumers.size());
        // 无消费者的消息不视为成功：抛可重试异常，由上下文置主记录 FAILED，待消费者注册后回灌
        AssertUtil.isTrueSystemError(CollUtil.isEmpty(consumers)).as(e -> MqConsumeException.retryable(e,
            I18nUtil.get("mq.v2.consume.no.consumer", context.getMessagePayloadType().getName()))).throwIt();
        List<MqConsumeVO<T>> consumeVos = new LinkedList<>();
        for (IMqConsumer<T> consumer : consumers) {
            Date startTime = new Date();
            log.debug("[MQ-v2] consume start, contextId={}, messageId={}, consumeId={}, businessId={}, consumer={}",
                contextId, messageVo.getMessageId(), consumer.consumeId(), consumer.businessId(),
                consumer.getClass().getName());
            try {
                consume(consumer, messageVo);
                log.debug("[MQ-v2] consume success, contextId={}, messageId={}, consumeId={}, costMs={}", contextId,
                    messageVo.getMessageId(), consumer.consumeId(), new Date().getTime() - startTime.getTime());
                consumeVos.add(buildConsumeSuccess(startTime, context, consumer, messageVo));
            } catch (Exception e) {
                log.warn("[MQ-v2] consume failed, contextId={}, messageId={}, consumeId={}, costMs={}", contextId,
                    messageVo.getMessageId(), consumer.consumeId(), new Date().getTime() - startTime.getTime(), e);
                consumeVos.add(buildConsumeFail(startTime, context, consumer, messageVo, e));
            }
        }
        log.debug("[MQ-v2] process done, contextId={}, messageId={}, ledgerRows={}", contextId, messageVo.getMessageId(),
            consumeVos.size());
        return consumeVos;
    }

    /**
     * 执行单个消费者的消费动作
     * <p>
     * 消息头携带操作人时以该身份执行，使消费链路内的审计字段与发起端一致；未携带、格式非法
     * 或未装配操作人上下文时按当前上下文执行（通常为系统主体）。
     * </p>
     *
     * @param consumer  命中的消费者
     * @param messageVo 消息主记录VO
     */
    private void consume(IMqConsumer<T> consumer, MqMessageIncomeVO<R, T> messageVo) {
        Runnable action = () -> consumer.consume(messageVo.getPayload());
        Long operatorId = resolveOperatorId(messageVo);
        if (operatorId == null || operatorContext == null) {
            action.run();
            return;
        }
        operatorContext.runAs(operatorId, action);
    }

    /**
     * 从消息头解析操作人ID
     *
     * @param messageVo 消息主记录VO
     * @return 操作人ID；未携带或格式非法时返回 null
     */
    private Long resolveOperatorId(MqMessageIncomeVO<R, T> messageVo) {
        Map<String, String> headers = messageVo.getHeaders();
        if (headers == null) {
            return null;
        }
        String operatorId = headers.get(MqHeaderConstants.OPERATOR_ID);
        if (StrUtil.isEmpty(operatorId)) {
            return null;
        }
        try {
            return Long.valueOf(operatorId);
        } catch (NumberFormatException e) {
            log.warn("[MQ-v2] invalid operator id header, skip restoring, messageId={}, value={}",
                messageVo.getMessageId(), operatorId);
            return null;
        }
    }
}
