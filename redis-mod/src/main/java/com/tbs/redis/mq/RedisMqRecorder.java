package com.tbs.redis.mq;

import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.mq.v2.core.IMqContext;
import com.tbs.mq.v2.core.IMqRecorder;
import com.tbs.mq.v2.enums.MqMessageStatusEnum;
import com.tbs.mq.v2.vo.MqConsumeVO;
import com.tbs.mq.v2.vo.MqMessageIncomeVO;
import com.tbs.mq.v2.vo.MqRecordVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.codec.SerializationCodec;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

/**
 * 消息记录器的 Redis 实现（v2 记录驱动模型 · 跨实例共享的可靠性真源）
 * <p>
 * 以"记录器ID + 上下文ID"为作用域隔离不同队列的归属，在 Redis 中维护与内存实现同构的两层记录：
 * <ul>
 *     <li>主记录：Hash {@code mq:recorder:{recorderId}:{contextId}:main}，field 为 messageId，
 *     {@link #record} 以 {@code HSETNX} 语义幂等建档、{@link #updateFlag} 推进状态、
 *     {@link #errorFlag} 置 FAILED 并留痕异常、{@link #get} 回查；</li>
 *     <li>消费台账：List {@code ...:ledger}，{@link #recordConsume} 追加每次分发的成/败行；
 *     失败留痕独立存放在 {@code ...:error} Hash。</li>
 * </ul>
 * 主记录与台账是重试扫描、人工补消费的依据：已处理记录按 3 天过期，其余均不设 TTL。
 * </p>
 * <p>
 * 过期策略：
 * <ul>
 *     <li>已处理（PROCESSED）的主记录在推进终态时迁入按小时分桶的完成层 {@code ...:done:{yyyyMMddHH}}，
 *     桶键 TTL 3 天，单条记录实际存活 72~73 小时后自动释放；</li>
 *     <li>未完成（RECEIVED/PROCESSING）与失败（FAILED）的主记录、失败留痕 {@code ...:error}、
 *     消费台账 {@code ...:ledger} 均不设 TTL——它们是重试扫描、人工补消费的唯一依据，过期即等于丢消息。</li>
 * </ul>
 * 完成层不参与回查（{@link #get} 只读主记录层），已处理消息无需重试/回灌。
 * </p>
 * <p>
 * 序列化说明：键用 {@link StringCodec} 保持冒号分层的纯文本、可巡检；值用 {@link SerializationCodec}
 * （JDK 序列化），而非模块常规的 JSON。原因是契约层 {@code MqMessageIncomeVO}/{@code MqConsumeVO}
 * 已强制 {@code Serializable}，且台账行携带 {@code Exception} 对象——JSON 无法无损往返异常实例。
 * 落库前会剥离厂商原始消息 {@code rawMessage}（见 {@link #detach}）。
 * </p>
 * <p>
 * 事务方法沿用接口默认的 no-op：Redis 无跨命令事务，状态推进与台账为逐条提交；
 * 进程崩溃时的恢复由主记录 PROCESSING 滞留扫描 + 整体重放承担（消费者必须幂等）。
 * </p>
 *
 * @param <R> 厂商原始消息类型（传输层）
 * @param <T> 业务负载类型
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public class RedisMqRecorder<R, T extends Serializable> implements IMqRecorder<R, T> {

    /**
     * 键前缀，冒号分层：mq:recorder:{recorderId}:{contextId}:{层}
     */
    private static final String KEY_PREFIX = "mq:recorder:";

    /**
     * 主记录层后缀
     */
    private static final String MAIN_SUFFIX = ":main";

    /**
     * 失败留痕层后缀
     */
    private static final String ERROR_SUFFIX = ":error";

    /**
     * 消费台账层后缀
     */
    private static final String LEDGER_SUFFIX = ":ledger";

    /**
     * 完成归档层后缀，后接按小时分桶的时间戳
     */
    private static final String DONE_SUFFIX = ":done:";

    /**
     * 完成记录保留时长：按小时分桶的桶键 TTL，单条记录实际存活 72~73 小时
     */
    private static final Duration DONE_TTL = Duration.ofDays(3);

    /**
     * 完成归档分桶时间格式：按小时分桶，避免整层 TTL 被后续写入不断刷新而永不淘汰
     */
    private static final DateTimeFormatter DONE_BUCKET_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH");

    /**
     * 值编解码器：JDK 序列化
     * <p>
     * 显式传入本类所在类加载器，避免 Netty 线程上下文类加载器解析不到业务负载类型。
     * </p>
     */
    private static final Codec VALUE_CODEC = new SerializationCodec(RedisMqRecorder.class.getClassLoader());

    /**
     * 键纯文本、值 JDK 序列化的复合编解码器
     * <p>
     * 三段参数不可省：Redisson 3.x 的 {@code CompositeCodec(Codec, Codec)} 两参构造只填 mapKey/mapValue，
     * {@code valueCodec} 留空，导致 List（台账）取 {@code getValueEncoder()} 时 NPE；故显式传三参使 value 段与非 Map 结构共用同一序列化器。
     * </p>
     */
    private static final Codec CODEC = new CompositeCodec(StringCodec.INSTANCE, VALUE_CODEC, VALUE_CODEC);

    /**
     * 缺省作用域：上下文未提供上下文ID时归入此桶，避免以 null 参与键拼接
     */
    private static final Long NULL_SCOPE = 0L;

    /**
     * Redisson 客户端，记录的实际载体
     */
    @Resource
    private RedissonClient redissonClient;

    /**
     * 记录器身份ID，多记录器共存时用于寻址，默认为 0
     */
    private Long recorderId = 0L;

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
     * 批量建档
     * <p>
     * 逐条写入上下文作用域的主记录 Hash，同 messageId 重复建档保留首条并仍回成功（幂等去重）， messageId 为空的条目回建档失败（宁拒勿漏）；每条入参各回一个结果项，供接收段按 messageId 配对。
     * </p>
     *
     * @param context  建档归属的消费上下文（队列级作用域）
     * @param messages 待建档的消息VO批次
     * @return 逐条建档结果列表，支持部分成功
     */
    @Override
    public List<MqRecordVO<T>> record(IMqContext<R, T> context, List<MqMessageIncomeVO<R, T>> messages) {
        List<MqRecordVO<T>> results = new LinkedList<>();
        if (CollUtil.isEmpty(messages)) {
            log.debug("[MQ-v2] redis recorder.record skip empty batch, key={}", mainKey(context));
            return results;
        }
        RMap<String, MqMessageIncomeVO<R, T>> main = mainRecords(context);
        int created = 0;
        int duplicated = 0;
        for (MqMessageIncomeVO<R, T> vo : messages) {
            MqRecordVO<T> result = new MqRecordVO<>();
            if (vo == null || StrUtil.isEmpty(vo.getMessageId())) {
                result.setMessageId(vo == null ? null : vo.getMessageId());
                result.setSuccess(false);
                result.setException(new IllegalStateException("redis record requires a non-empty messageId"));
                results.add(result);
                log.debug("[MQ-v2] redis recorder.record reject blank messageId, key={}", mainKey(context));
                continue;
            }
            // ponytail: 逐条 HSETNX，批大小在百级时仍是一次批量建档内 N 次往返；若成为瓶颈改用 RBatch 一次发送
            boolean isNew = main.putIfAbsent(vo.getMessageId(), detach(vo)) == null;
            if (isNew) {
                created++;
            } else {
                duplicated++;
            }
            result.setMessageId(vo.getMessageId());
            result.setBody(vo.getPayload());
            result.setSuccess(true);
            results.add(result);
        }
        log.debug("[MQ-v2] redis recorder.record done, key={}, total={}, created={}, duplicated={}", mainKey(context),
            messages.size(), created, duplicated);
        return results;
    }

    /**
     * 推进主记录状态
     * <p>
     * 读改写非原子，但同一 messageId 的处理由单飞抢占器串行保护（见 {@code IMqMessageMonopolizer}）， 状态推进不会并发交错。
     * PROCESSED 为正常消费完成的终态：记录迁入完成层（3 天过期）并从主记录层移除，主记录层只保留未完成与失败记录。
     * </p>
     *
     * @param context   当前上下文
     * @param messageId 消息ID
     * @param flag      目标状态
     */
    @Override
    public void updateFlag(IMqContext<R, T> context, String messageId, MqMessageStatusEnum flag) {
        if (flag == null || StrUtil.isEmpty(messageId)) {
            log.debug("[MQ-v2] redis recorder.updateFlag skip, blank flag/messageId, messageId={}", messageId);
            return;
        }
        RMap<String, MqMessageIncomeVO<R, T>> main = mainRecords(context);
        MqMessageIncomeVO<R, T> vo = main.get(messageId);
        if (vo == null) {
            log.debug("[MQ-v2] redis recorder.updateFlag miss, key={}, messageId={}", mainKey(context), messageId);
            return;
        }
        vo.setFlag(flag.getCode());
        if (MqMessageStatusEnum.PROCESSED == flag) {
            archiveProcessed(context, messageId, vo);
            return;
        }
        main.put(messageId, vo);
        log.debug("[MQ-v2] redis recorder.updateFlag to {}, key={}, messageId={}", flag, mainKey(context), messageId);
    }

    /**
     * 归档已处理记录：先写入完成层并设 TTL，再从主记录层删除
     * <p>
     * 顺序不可颠倒：先立后删，两命令间崩溃时记录同时存在于两层（无害）；反序一旦崩溃则记录丢失。
     * 每次写入重置桶 TTL，同小时内写入的记录最多多存活 1 小时。
     * </p>
     *
     * @param context   当前上下文
     * @param messageId 消息ID
     * @param vo        已推进为 PROCESSED 的主记录
     */
    private void archiveProcessed(IMqContext<R, T> context, String messageId, MqMessageIncomeVO<R, T> vo) {
        String doneKey = doneKey(context);
        RMap<String, MqMessageIncomeVO<R, T>> done = redissonClient.getMap(doneKey, CODEC);
        done.put(messageId, vo);
        done.expire(DONE_TTL);
        mainRecords(context).remove(messageId);
        log.debug("[MQ-v2] redis recorder.archive PROCESSED, key={}, messageId={}, ttlDays={}", doneKey, messageId,
            DONE_TTL.toDays());
    }

    /**
     * 置主记录为 FAILED 并留痕失败原因
     * <p>
     * 状态先落、异常后落：异常对象可能携带不可序列化字段，留痕失败只记日志，不影响 FAILED 状态的可扫描性。
     * </p>
     *
     * @param context   当前上下文
     * @param messageId 消息ID
     * @param e         失败原因异常
     */
    @Override
    public void errorFlag(IMqContext<R, T> context, String messageId, Exception e) {
        if (StrUtil.isEmpty(messageId)) {
            log.debug("[MQ-v2] redis recorder.errorFlag skip, blank messageId");
            return;
        }
        updateFlag(context, messageId, MqMessageStatusEnum.FAILED);
        try {
            errorStore(context).put(messageId, e);
        } catch (Exception storeError) {
            log.error("[MQ-v2] redis recorder.errorFlag persist exception failed, key={}, messageId={}",
                errorKey(context), messageId, storeError);
        }
        log.debug("[MQ-v2] redis recorder.errorFlag to FAILED, key={}, messageId={}", mainKey(context), messageId, e);
    }

    /**
     * 批量追加消费台账行
     * <p>
     * 空元素跳过（Redis List 不接受 null 值）；整批一次性追加，写入失败向上抛出， 由处理段按"分发异常"走主记录 FAILED 并整体重放，不静默丢台账。
     * </p>
     *
     * @param context 当前上下文
     * @param record  消费台账批次
     * @return 落库成功的行数
     */
    @Override
    public int recordConsume(IMqContext<R, T> context, List<MqConsumeVO<T>> record) {
        if (CollUtil.isEmpty(record)) {
            log.debug("[MQ-v2] redis recorder.recordConsume skip empty ledger, key={}", ledgerKey(context));
            return 0;
        }
        List<MqConsumeVO<T>> rows = new LinkedList<>();
        for (MqConsumeVO<T> row : record) {
            if (row != null) {
                rows.add(row);
            }
        }
        if (rows.isEmpty()) {
            log.debug("[MQ-v2] redis recorder.recordConsume skip all-null ledger, key={}", ledgerKey(context));
            return 0;
        }
        RList<MqConsumeVO<T>> ledger = redissonClient.getList(ledgerKey(context), CODEC);
        ledger.addAll(rows);
        log.debug("[MQ-v2] redis recorder.recordConsume append {} rows, key={}", rows.size(), ledgerKey(context));
        return rows.size();
    }

    /**
     * 按消息ID回查主记录
     * <p>
     * 返回的是 Redis 反序列化副本，{@code rawMessage} 已在建档时剥离（厂商原始消息不落库）， 重试回灌应使用 {@code payload} 与 {@code headers}。
     * 只读主记录层：已处理（PROCESSED）的记录已迁入完成层，此处返回 null（它们无需重试/回灌）。
     * </p>
     *
     * @param context   当前上下文
     * @param messageId 消息ID
     * @return 消息主记录VO，不存在时返回 null
     */
    @Override
    public MqMessageIncomeVO<R, T> get(IMqContext<R, T> context, String messageId) {
        if (StrUtil.isEmpty(messageId)) {
            log.debug("[MQ-v2] redis recorder.get skip, blank messageId");
            return null;
        }
        MqMessageIncomeVO<R, T> vo = mainRecords(context).get(messageId);
        log.debug("[MQ-v2] redis recorder.get {}, key={}, messageId={}", vo == null ? "miss" : "hit", mainKey(context),
            messageId);
        return vo;
    }

    /**
     * 剥离厂商原始消息后复制主记录
     * <p>
     * 厂商原始消息（如 Spring AMQP Message）不保证可序列化且体积大，仅用于当次消费的确认与排查， 故不进入 Redis；业务负载与消息头完整保留，满足回灌需要。
     * </p>
     *
     * @param vo 待落库的主记录
     * @return 可序列化的主记录副本
     */
    private MqMessageIncomeVO<R, T> detach(MqMessageIncomeVO<R, T> vo) {
        return MqMessageIncomeVO.<R, T>builder().messageId(vo.getMessageId()).businessId(vo.getBusinessId())
            .payload(vo.getPayload()).headers(vo.getHeaders()).flag(vo.getFlag()).build();
    }

    /**
     * 取主记录 Hash
     *
     * @param context 当前上下文
     * @return messageId → 主记录 的 Hash
     */
    private RMap<String, MqMessageIncomeVO<R, T>> mainRecords(IMqContext<R, T> context) {
        return redissonClient.getMap(mainKey(context), CODEC);
    }

    /**
     * 取失败留痕 Hash
     *
     * @param context 当前上下文
     * @return messageId → 失败异常 的 Hash
     */
    private RMap<String, Exception> errorStore(IMqContext<R, T> context) {
        return redissonClient.getMap(errorKey(context), CODEC);
    }

    /**
     * 主记录键
     *
     * @param context 当前上下文
     * @return Redis 键
     */
    private String mainKey(IMqContext<R, T> context) {
        return scopeKey(context) + MAIN_SUFFIX;
    }

    /**
     * 失败留痕键
     *
     * @param context 当前上下文
     * @return Redis 键
     */
    private String errorKey(IMqContext<R, T> context) {
        return scopeKey(context) + ERROR_SUFFIX;
    }

    /**
     * 消费台账键
     *
     * @param context 当前上下文
     * @return Redis 键
     */
    private String ledgerKey(IMqContext<R, T> context) {
        return scopeKey(context) + LEDGER_SUFFIX;
    }

    /**
     * 完成归档键：作用域前缀 + 当前小时桶
     *
     * @param context 当前上下文
     * @return Redis 键
     */
    private String doneKey(IMqContext<R, T> context) {
        return scopeKey(context) + DONE_SUFFIX + LocalDateTime.now().format(DONE_BUCKET_FORMAT);
    }

    /**
     * 作用域键：记录器ID + 上下文ID，隔离不同记录器与不同队列的归属
     *
     * @param context 当前上下文
     * @return 作用域键前缀
     */
    private String scopeKey(IMqContext<R, T> context) {
        Long contextId = context == null ? null : context.getContextId();
        return KEY_PREFIX + recorderId + ":" + (contextId == null ? NULL_SCOPE : contextId);
    }
}
