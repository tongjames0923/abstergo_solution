package com.tbs.push.api.dispatch;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.push.api.coordinator.IRouteCoordinator;
import com.tbs.push.api.frame.IPushCodec;
import com.tbs.push.api.record.IDeliveryRecordStore;
import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.api.store.IPushMessageStore;
import com.tbs.push.constants.PushI18nConstant;
import com.tbs.push.model.message.DeliveryRecord;
import com.tbs.push.model.message.DeliveryResultEnum;
import com.tbs.push.model.message.PushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 推送消息分发器基类
 * <p>
 * 将"取消息 → 解析目标 → 逐会话投递 → 结果留痕"的公共骨架固化为模板方法：
 * <ol>
 *     <li>目标解析：经{@link IRouteCoordinator}解析投递目标会话集合</li>
 *     <li>逐会话投递：统一会话发送DATA帧，单会话失败不中断其余目标</li>
 *     <li>结果留痕：每次投递动作（含无目标场景）写一条完整投递记录</li>
 * </ol>
 * 投递一次即可：发送失败仅如实记录，不做自动重试与补发。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public abstract class AbstractPushDispatcher implements IPushDispatcher {

    protected static final Logger log = LoggerFactory.getLogger(AbstractPushDispatcher.class);

    /**
     * 默认单轮取出条数
     */
    public static final int DEFAULT_FETCH_BATCH_SIZE = 50;

    /**
     * 投递协调器（必填）
     */
    protected final IRouteCoordinator routeCoordinator;

    /**
     * 协议编解码器（必填）
     */
    protected final IPushCodec codec;

    /**
     * 投递记录存储，未注入时不留痕
     */
    protected IDeliveryRecordStore deliveryRecordStore;

    /**
     * 单轮取出条数
     */
    protected int fetchBatchSize = DEFAULT_FETCH_BATCH_SIZE;

    /**
     * 构造分发器基类
     *
     * @param routeCoordinator 投递协调器，不能为空
     * @param codec            协议编解码器，不能为空
     */
    protected AbstractPushDispatcher(IRouteCoordinator routeCoordinator, IPushCodec codec) {
        AssertUtil.isTrue(routeCoordinator == null)
            .i18n(PushI18nConstant.ROUTE_COORDINATOR_REQUIRED).throwIt();
        AssertUtil.isTrue(codec == null).i18n(PushI18nConstant.CODEC_REQUIRED).throwIt();
        this.routeCoordinator = routeCoordinator;
        this.codec = codec;
    }

    /**
     * 分发单条消息（模板方法）
     * <p>
     * 解析目标 → 逐会话投递 → 结果留痕。本方法不向调用方抛出投递类异常。
     * </p>
     *
     * @param message 待分发的业务消息
     */
    @Override
    public void dispatch(PushMessage message) {
        AssertUtil.isTrue(message == null).i18n(PushI18nConstant.MESSAGE_REQUIRED).throwIt();
        List<IConnectionSession> targets = routeCoordinator.resolveTargets(message);
        if (CollUtil.isEmpty(targets)) {
            record(message, null, DeliveryResultEnum.NO_SUBSCRIBER, null);
            return;
        }
        for (IConnectionSession target : targets) {
            deliver(message, target);
        }
    }

    /**
     * 向单个目标会话投递并留痕
     *
     * @param message 待投递的业务消息
     * @param target  目标会话
     */
    protected void deliver(PushMessage message, IConnectionSession target) {
        boolean sent;
        String errorMessage = null;
        try {
            sent = target.send(codec.encodeMessage(message));
        } catch (Throwable error) {
            sent = false;
            errorMessage = error.getMessage();
        }
        record(message, target, sent ? DeliveryResultEnum.DELIVERED : DeliveryResultEnum.FAILED, errorMessage);
        if (!sent) {
            // 失败明细已在 record() 留痕；此处降为 debug，避免断链会话在消息洪峰下刷屏
            log.debug("消息投递失败, messageId:{}, topic:{}, sessionId:{}, transport:{}",
                message.getId(), message.getTopic(), target.getSessionId(), target.getTransportType());
        }
    }

    /**
     * 追加投递记录
     * <p>
     * 记录存储未注入时跳过留痕；留痕自身异常不影响投递主流程。
     * </p>
     *
     * @param message      关联的业务消息
     * @param target       目标会话，无目标时为null
     * @param result       投递结果
     * @param errorMessage 失败原因摘要
     */
    protected void record(PushMessage message, IConnectionSession target, DeliveryResultEnum result,
        String errorMessage) {
        if (deliveryRecordStore == null) {
            return;
        }
        try {
            DeliveryRecord record = new DeliveryRecord();
            record.setMessageId(message.getId());
            record.setTopic(message.getTopic());
            if (target != null) {
                record.setSessionId(target.getSessionId());
                record.setTransportType(target.getTransportType());
            }
            record.setResult(result);
            record.setErrorMessage(errorMessage);
            deliveryRecordStore.append(record);
        } catch (Throwable error) {
            log.warn("投递记录写入失败, messageId:{}", message.getId(), error);
        }
    }

    /**
     * 从消息源取出一批消息并逐条分发
     * <p>
     * 消息源由子类提供，未装配时整轮静默跳过。
     * </p>
     */
    @Override
    public void dispatchDue() {
        IPushMessageStore store = getMessageStore();
        if (store == null) {
            return;
        }
        List<PushMessage> pending = store.fetchPending(fetchBatchSize);
        if (CollUtil.isEmpty(pending)) {
            return;
        }
        for (PushMessage message : pending) {
            if (message == null || StrUtil.isEmpty(message.getId())) {
                continue;
            }
            dispatch(message);
            store.markDispatched(message.getId());
        }
    }

    /**
     * 分发一轮消息（异常安全）
     */
    @Override
    public void dispatchDueSafely() {
        try {
            dispatchDue();
        } catch (Exception error) {
            log.error("推送消息分发调度异常", error);
        }
    }

    /**
     * 获取消息源
     *
     * @return 消息源，未装配时返回null
     */
    protected abstract IPushMessageStore getMessageStore();

    /**
     * 注入投递记录存储
     *
     * @param deliveryRecordStore 投递记录存储
     * @return 当前对象
     */
    public AbstractPushDispatcher deliveryRecordStore(IDeliveryRecordStore deliveryRecordStore) {
        this.deliveryRecordStore = deliveryRecordStore;
        return this;
    }

    /**
     * 设置单轮取出条数
     *
     * @param fetchBatchSize 单轮取出条数，非正数按默认处理
     * @return 当前对象
     */
    public AbstractPushDispatcher fetchBatchSize(int fetchBatchSize) {
        this.fetchBatchSize = fetchBatchSize > 0 ? fetchBatchSize : DEFAULT_FETCH_BATCH_SIZE;
        return this;
    }

}
