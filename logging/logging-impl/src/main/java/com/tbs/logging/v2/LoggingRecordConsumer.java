package com.tbs.logging.v2;

import cn.hutool.core.collection.CollUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.JsonUtils;
import com.tbs.logging.form.LoggingRecordForm;
import com.tbs.mq.v2.consume.IMqConsumer;
import com.tbs.mq.v2.exception.MqConsumeException;
import com.tbs.shared.mapper.SystemOperateMapper;
import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 日志记录消费者（MQ v2 记录驱动模型 · 承载缓冲+批量入库的业务消费单元）
 * <p>
 * 实现 {@link IMqConsumer}，由日志 v2 上下文在其处理线程内、单飞保护与 PROCESSING 状态下调用 {@link #consume}。
 * 沿用原 v1 日志写入器的高吞吐范式：本地缓冲队列 + 满批/刷新信号触发批量入库，
 * 单条失败降级逐条写入，不因个别异常阻断整体（日志链路容忍丢失）。消费只面向负载 {@link LoggingRecordForm}，
 * 不感知厂商通道、不做任何 ack/nak（确认由框架在接收段完成）。
 * </p>
 * <p>
 * 接入模型为 v2：本类是注册进 {@code IConsumerRegistry} 的 v2 消费者，可与其他消费者按负载类型广播共存。
 * 非 Spring 注解类，由 config 装配并注册。
 * </p>
 *
 * @author tongj
 * @since 2.0.0
 */
@Slf4j
public class LoggingRecordConsumer implements IMqConsumer<LoggingRecordForm> {

    /**
     * 批量写入的批次大小
     */
    private static final int BATCH_SIZE = 100;

    /**
     * 缓冲队列容量，写满时强制刷新
     */
    private static final int BUFFER_CAPACITY = 100;

    /**
     * 定时刷新间隔（毫秒）：低于批量阈值滞留的记录，最迟经此间隔落库，兼顾吞吐与新鲜度
     */
    private static final long FLUSH_INTERVAL_MILLIS = 5000L;

    /**
     * 刷新标志值：{@link LoggingRecordForm#getFlushFlag()} 等于该值表示请求立即落库
     */
    private static final int FLUSH_FLAG_VALUE = 1;

    /**
     * AjaxResult 成功码（响应体统一包装约定：{"map":{"code":200,...}}）
     */
    private static final int RESULT_SUCCESS_CODE = 200;

    /**
     * 消费者业务id
     */
    private static final String BUSINESS_ID = "systemLog";

    /**
     * 消费者id，同一负载类型内全局唯一，作为消费台账行身份键
     */
    private static final Integer CONSUME_ID = 1;

    /**
     * 日志记录数据访问
     */
    private final SystemOperateMapper mapper;

    /**
     * 缓冲队列，暂存待写入的日志实体
     */
    private final BlockingQueue<SystemOperateRecordEntity> bufferQueue = new LinkedBlockingQueue<>(BUFFER_CAPACITY);

    /**
     * 定时刷库调度器：单守护线程，固定间隔触发 {@link #flushBuffer}，避免未满批记录无限滞留
     */
    private ScheduledExecutorService flushScheduler;

    /**
     * 构造日志记录消费者
     *
     * @param mapper 日志记录数据访问，不为空
     */
    public LoggingRecordConsumer(SystemOperateMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 启动定时刷库调度器：以固定间隔轮询刷新缓冲区，保障未满批的记录不至于长期滞留
     */
    @PostConstruct
    public void startFlushScheduler() {
        ThreadFactory daemonFactory = runnable -> {
            Thread thread = new Thread(runnable, "mq-v2-log-flush");
            thread.setDaemon(true);
            return thread;
        };
        flushScheduler = Executors.newSingleThreadScheduledExecutor(daemonFactory);
        flushScheduler.scheduleWithFixedDelay(this::flushBufferQuietly, FLUSH_INTERVAL_MILLIS, FLUSH_INTERVAL_MILLIS,
            TimeUnit.MILLISECONDS);
        log.debug("[MQ-v2][log] flush scheduler started, intervalMs={}", FLUSH_INTERVAL_MILLIS);
    }

    /**
     * 停止定时刷库调度器，并尽力刷尽残留
     */
    @PreDestroy
    public void stopFlushScheduler() {
        if (flushScheduler != null) {
            flushScheduler.shutdown();
        }
        flushBufferQuietly();
        log.debug("[MQ-v2][log] flush scheduler stopped");
    }

    /**
     * 调度线程入口：屏蔽 flushBuffer 可能抱出的意外异常，避免异常导致周期任务静默终止
     */
    private void flushBufferQuietly() {
        try {
            flushBuffer();
        } catch (Exception e) {
            log.error("定时刷新日志缓冲区失败", e);
        }
    }

    /**
     * 消费者业务id
     *
     * @return 归属业务域标识
     */
    @Override
    public String businessId() {
        return BUSINESS_ID;
    }

    /**
     * 消费者id
     *
     * @return 消费台账行身份键
     */
    @Override
    public Integer consumeId() {
        return CONSUME_ID;
    }

    /**
     * 消费单条日志负载：刷新信号立即落库，否则转换入缓冲，缓冲写满则强制刷批
     *
     * @param message 业务负载
     * @throws MqConsumeException 消费失败（缓冲范式内部已兜底降级，一般不抛出）
     */
    @Override
    public void consume(LoggingRecordForm message) throws MqConsumeException {
        if (message == null) {
            log.debug("[MQ-v2][log] consume skip null payload, consumeId={}", CONSUME_ID);
            return;
        }
        if (message.getFlushFlag() != null && message.getFlushFlag() == FLUSH_FLAG_VALUE) {
            log.debug("[MQ-v2][log] consume flush signal, consumeId={}, bufferSize={}", CONSUME_ID, bufferQueue.size());
            flushBuffer();
            return;
        }
        SystemOperateRecordEntity entity = convertToEntity(message);
        if (entity == null) {
            log.debug("[MQ-v2][log] consume entity null after convert, consumeId={}", CONSUME_ID);
            return;
        }
        entity.setSystemLogFlag(1);
        if (!bufferQueue.offer(entity)) {
            log.warn("日志缓冲队列已满，强制刷新");
            flushBuffer();
            bufferQueue.offer(entity);
        } else {
            log.debug("[MQ-v2][log] consume buffered, consumeId={}, traceId={}, bufferSize={}", CONSUME_ID,
                message.getTraceId(), bufferQueue.size());
        }
    }

    /**
     * 刷新缓冲区，批量写入数据库；批写失败降级为逐条写入。
     *
     * <p>本方法<b>有意不加事务</b>：{@code insertList} 按方言上限分批执行、不构成原子操作，而操作日志链路
     * 容忍部分丢失。若把整批纳入事务，一条脏数据会连带回滚已成功的批次，把失败半径放大到整批日志；
     * 故保留「批写失败即降级逐条、单条失败仅记日志」的尽力写入语义（见 03 分册 3.3.2 第 2 条例外口径）。</p>
     */
    private void flushBuffer() {
        if (bufferQueue.isEmpty()) {
            log.debug("[MQ-v2][log] flush skip, buffer empty");
            return;
        }
        List<SystemOperateRecordEntity> batch = new ArrayList<>(BATCH_SIZE);
        int drained = bufferQueue.drainTo(batch, BATCH_SIZE);
        if (CollUtil.isEmpty(batch)) {
            log.debug("[MQ-v2][log] flush skip, drained 0");
            return;
        }
        log.debug("[MQ-v2][log] flush drained {} record(s), remainingBufferSize={}", drained, bufferQueue.size());
        try {
            mapper.insertList(batch);
            log.debug("[MQ-v2][log] flush batch inserted, size={}", batch.size());
        } catch (Exception e) {
            log.error("批量写入日志记录失败，降级逐条写入，数量: {}", batch.size(), e);
            for (SystemOperateRecordEntity entity : batch) {
                try {
                    mapper.insertSelective(entity);
                } catch (Exception ex) {
                    log.error("单条日志写入失败", ex);
                }
            }
        }
    }

    /**
     * 将日志表单转换为持久化实体
     *
     * @param form 日志记录表单
     * @return 实体对象；form 为空时返回 null
     */
    private SystemOperateRecordEntity convertToEntity(LoggingRecordForm form) {
        if (form == null) {
            return null;
        }
        SystemOperateRecordEntity entity = new SystemOperateRecordEntity();
        entity.setOperateType(form.getOperateType());
        entity.setOperateContent(form.getOperateDesc());
        entity.setOperateUserName(form.getOperateUser());
        entity.setOperateIp(form.getOperateIp());
        entity.setOperateTime(form.getOperateTime());
        entity.setOperateResultJson(form.getOperateResult());
        entity.setOperateParamJson(form.getOperateParam());
        entity.setOperateError(form.getErrorMessage());
        entity.setOperateErrorLevel(resolveErrorLevel(form));
        entity.setOperateUrl(form.getOperateUrl());
        entity.setMethod(form.getMethod());
        entity.setTraceId(form.getTraceId());
        if (form.getOperateTime() != null && form.getOperateFinishTime() != null) {
            entity.setCostTime(form.getOperateFinishTime().getTime() - form.getOperateTime().getTime());
        }
        return entity;
    }

    /**
     * 解析错误码：异常路径沿用表单携带的 errorCode；无异常但业务返回体 map.code 非成功码按业务逻辑错误记录；其余为成功（null）
     *
     * @param form 日志记录表单
     * @return 错误码；成功时为 null
     */
    private Integer resolveErrorLevel(LoggingRecordForm form) {
        if (form.getErrorCode() != null) {
            return form.getErrorCode();
        }
        if (isBusinessFailure(form.getOperateResult())) {
            return ErrorLevelEnum.BUSINESS_LOGIC_ERROR.getCode();
        }
        return null;
    }

    /**
     * 判断正常返回的 AjaxResult 序列化体是否为业务失败（map.code 存在且非成功码）
     *
     * @param operateResultJson 操作结果 JSON
     * @return true 表示业务失败
     */
    private boolean isBusinessFailure(String operateResultJson) {
        if (operateResultJson == null || operateResultJson.isEmpty()) {
            return false;
        }
        // AjaxResult 包装体恒为 JSON 对象 {"map":{...}}；数组/标量等非对象结果不含 map.code，
        // 直接判为非业务失败，并从源头避免把非对象 JSON 喂给 JsonUtils.parseMap 触发其内部 ERROR 日志
        String trimmed = operateResultJson.trim();
        if (trimmed.isEmpty() || trimmed.charAt(0) != '{') {
            return false;
        }
        Map<String, Object> root = JsonUtils.parseMap(trimmed);
        Object mapObj = root == null ? null : root.get("map");
        if (mapObj instanceof Map) {
            Object code = ((Map<?, ?>) mapObj).get("code");
            return code != null && !String.valueOf(RESULT_SUCCESS_CODE).equals(String.valueOf(code));
        }
        return false;
    }
}
