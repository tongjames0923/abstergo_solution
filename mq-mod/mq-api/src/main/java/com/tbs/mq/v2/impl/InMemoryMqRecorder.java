package com.tbs.mq.v2.impl;

import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.mq.v2.core.IMqContext;
import com.tbs.mq.v2.core.IMqRecorder;
import com.tbs.mq.v2.enums.MqMessageStatusEnum;
import com.tbs.mq.v2.vo.MqConsumeVO;
import com.tbs.mq.v2.vo.MqMessageVO;
import com.tbs.mq.v2.vo.MqRecordVO;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 消息记录器的内存基本实现（v2 记录驱动模型 · 可靠性真源的进程内落地）
 * <p>
 * 以"上下文ID"为一级作用域隔离不同队列的归属，进程内维护两层记录：
 * <ul>
 *     <li>主记录：{@code contextId → (messageId → MqMessageVO)}，{@link #record} 建档（幂等：同 ID 重复建档
 *     保留首条并仍回成功）、{@link #updateFlag} 推进状态、{@link #errorFlag} 置 FAILED 并留痕异常、{@link #get} 回查；</li>
 *     <li>消费台账：{@code contextId → List<MqConsumeVO>}，{@link #recordConsume} 追加每次分发的成/败行。</li>
 * </ul>
 * 事务方法沿用接口默认的 no-op（内存无跨语句原子性需求）。所有结构均为并发安全容器，适配接收线程与处理线程并发访问。
 * </p>
 * <p>
 * 定位为单机/开发/自检用开箱即用实现，进程重启后记录清空、不跨实例共享；生产环境应替换为基于 DB 的
 * 记录器实现（主记录 + 台账两张表、事务与重试扫描）。
 * </p>
 *
 * @param <R> 厂商原始消息类型（传输层）
 * @param <T> 业务负载类型
 * @author tongj
 * @since 2.0.0
 */
public class InMemoryMqRecorder<R, T extends Serializable> implements IMqRecorder<R, T> {

    /**
     * 缺省作用域桶：当上下文未提供上下文ID时归入此桶，避免以 null 作并发Map键
     */
    private static final Long NULL_SCOPE = Long.MIN_VALUE;

    /**
     * 记录器身份ID，多记录器共存时用于寻址，默认为 0
     */
    private Long recorderId = 0L;

    /**
     * 主记录存储：上下文ID → (messageId → 消息主记录VO)
     */
    private final Map<Long, Map<String, MqMessageVO<R, T>>> mainRecords = new ConcurrentHashMap<>();

    /**
     * 失败留痕：上下文ID → (messageId → 最近一次失败异常)
     */
    private final Map<Long, Map<String, Exception>> errorStore = new ConcurrentHashMap<>();

    /**
     * 消费台账：上下文ID → 台账行列表（追加式，供回灌/排查读取）
     */
    private final Map<Long, List<MqConsumeVO<T>>> consumeLedgers = new ConcurrentHashMap<>();

    /**
     * 获取记录器ID
     *
     * @return 记录器ID
     */
    @Override
    public Long getRecorderId() {
        return recorderId;
    }

    /**
     * 设置记录器ID（供装配期注入，多记录器共存时区分身份）
     *
     * @param recorderId 记录器ID
     */
    public void setRecorderId(Long recorderId) {
        this.recorderId = recorderId;
    }

    /**
     * 批量建档：逐条落入上下文作用域的主记录表，按 messageId 幂等去重，返回与入参逐条配对的结果
     *
     * @param context  建档归属的消费上下文（队列级作用域）
     * @param messages 待建档的消息VO批次
     * @return 逐条建档结果列表；messageId 缺失的条目回失败结果（宁拒勿漏）
     */
    @Override
    public List<MqRecordVO<T>> record(IMqContext<R, T> context, List<MqMessageVO<R, T>> messages) {
        List<MqRecordVO<T>> results = new LinkedList<>();
        if (CollUtil.isEmpty(messages)) {
            return results;
        }
        Map<String, MqMessageVO<R, T>> store = mainStoreOf(context);
        for (MqMessageVO<R, T> vo : messages) {
            MqRecordVO<T> result = new MqRecordVO<>();
            if (vo == null || StrUtil.isEmpty(vo.getMessageId())) {
                result.setMessageId(vo == null ? null : vo.getMessageId());
                result.setSuccess(false);
                result.setException(new IllegalStateException("in-memory record requires a non-empty messageId"));
                results.add(result);
                continue;
            }
            store.putIfAbsent(vo.getMessageId(), vo);
            result.setMessageId(vo.getMessageId());
            result.setBody(vo.getPayload());
            result.setSuccess(true);
            results.add(result);
        }
        return results;
    }

    /**
     * 推进主记录状态
     *
     * @param context   当前上下文
     * @param messageId 消息ID
     * @param flag      目标状态
     */
    @Override
    public void updateFlag(IMqContext<R, T> context, String messageId, MqMessageStatusEnum flag) {
        if (flag == null || StrUtil.isEmpty(messageId)) {
            return;
        }
        MqMessageVO<R, T> vo = mainStoreOf(context).get(messageId);
        if (vo != null) {
            vo.setFlag(flag.getCode());
        }
    }

    /**
     * 置主记录为 FAILED 并留痕失败原因
     *
     * @param context   当前上下文
     * @param messageId 消息ID
     * @param e         失败原因异常
     */
    @Override
    public void errorFlag(IMqContext<R, T> context, String messageId, Exception e) {
        if (StrUtil.isEmpty(messageId)) {
            return;
        }
        MqMessageVO<R, T> vo = mainStoreOf(context).get(messageId);
        if (vo != null) {
            vo.setFlag(MqMessageStatusEnum.FAILED.getCode());
        }
        errorStoreOf(context).put(messageId, e);
    }

    /**
     * 批量追加消费台账行
     *
     * @param context 当前上下文
     * @param record  消费台账批次
     * @return 追加的行数
     */
    @Override
    public int recordConsume(IMqContext<R, T> context, List<MqConsumeVO<T>> record) {
        if (CollUtil.isEmpty(record)) {
            return 0;
        }
        List<MqConsumeVO<T>> ledger = consumeLedgers.computeIfAbsent(scopeKey(context),
            k -> new CopyOnWriteArrayList<>());
        ledger.addAll(record);
        return record.size();
    }

    /**
     * 按消息ID回查主记录
     *
     * @param context   当前上下文
     * @param messageId 消息ID
     * @return 消息主记录VO，不存在时返回 null
     */
    @Override
    public MqMessageVO<R, T> get(IMqContext<R, T> context, String messageId) {
        if (StrUtil.isEmpty(messageId)) {
            return null;
        }
        return mainStoreOf(context).get(messageId);
    }

    /**
     * 取当前作用域键：以上下文ID隔离归属，空上下文归入 null 键
     *
     * @param context 当前上下文
     * @return 作用域键
     */
    private Long scopeKey(IMqContext<R, T> context) {
        Long id = context == null ? null : context.getContextId();
        return id == null ? NULL_SCOPE : id;
    }

    /**
     * 取得（惰性创建）当前上下文的主记录子表
     *
     * @param context 当前上下文
     * @return messageId → 主记录VO 的并发子表
     */
    private Map<String, MqMessageVO<R, T>> mainStoreOf(IMqContext<R, T> context) {
        return mainRecords.computeIfAbsent(scopeKey(context), k -> new ConcurrentHashMap<>());
    }

    /**
     * 取得（惰性创建）当前上下文的失败留痕子表
     *
     * @param context 当前上下文
     * @return messageId → 最近失败异常 的并发子表
     */
    private Map<String, Exception> errorStoreOf(IMqContext<R, T> context) {
        return errorStore.computeIfAbsent(scopeKey(context), k -> new ConcurrentHashMap<>());
    }
}
