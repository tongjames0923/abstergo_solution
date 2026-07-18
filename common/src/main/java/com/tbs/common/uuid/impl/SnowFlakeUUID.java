package com.tbs.common.uuid.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.uuid.IUuidGenerator;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 雪花算法UUID生成器（CAS无锁版）
 *
 * @author abstergo
 */
public class SnowFlakeUUID implements IUuidGenerator {

    // ===================== 位分配常量 =====================
    private static final long TWEPOCH = 1609459200000L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATA_CENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    private static final long MAX_TIMESTAMP_DIFF = ~(-1L << (64L - WORKER_ID_BITS - DATA_CENTER_ID_BITS - SEQUENCE_BITS - 1));

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;
    private static final long SEQUENCE_MASK = MAX_SEQUENCE;

    // ===================== 实例字段 =====================
    private final long workerId;
    private final long dataCenterId;
    private final AtomicLong tsSequence = new AtomicLong(0L);

    /**
     * 构造雪花UUID生成器
     *
     * @param workerId     机器ID [0,31]
     * @param dataCenterId 数据中心ID [0,31]
     */
    public SnowFlakeUUID(long workerId, long dataCenterId) {
        AssertUtil.isTrue(workerId < 0 || workerId > MAX_WORKER_ID,
            I18nUtil.get("snowflake.worker.id.invalid", MAX_WORKER_ID, workerId));
        AssertUtil.isTrue(dataCenterId < 0 || dataCenterId > MAX_DATA_CENTER_ID,
            I18nUtil.get("snowflake.datacenter.id.invalid", MAX_DATA_CENTER_ID, dataCenterId));
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    public SnowFlakeUUID() {
        this(autoWorkerId(), autoDataCenterId());
    }

    /**
     * 基于本机IP最后一段自动计算workerId，对32取模映射到 [0,31]
     */
    private static long autoWorkerId() {
        try {
            byte[] ip = InetAddress.getLocalHost().getAddress();
            return (ip[3] & 0xFF) % (MAX_WORKER_ID + 1);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 基于server.port自动计算dataCenterId，对32取模映射到 [0,31] 优先读取JVM系统属性（Spring Boot自动设置），其次读取环境变量
     */
    private static long autoDataCenterId() {
        String port = System.getProperty("server.port");
        if (port == null || port.isBlank()) {
            port = System.getenv("SERVER_PORT");
        }
        if (port != null && !port.isBlank()) {
            try {
                return Long.parseLong(port.trim()) % (MAX_DATA_CENTER_ID + 1);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0L;
    }

    // ===================== 核心生成方法 =====================

    /**
     * 无锁线程安全生成唯一雪花ID（基于 CAS 替代 synchronized）
     *
     * @return 正数long ID，溢出直接抛异常，不会返回负数
     */
    public long nextId() {
        long currentTs;
        long packed;
        long lastTs;
        long seq;

        while (true) {
            packed = tsSequence.get();
            lastTs = packed >>> SEQUENCE_BITS;
            seq = packed & SEQUENCE_MASK;

            currentTs = System.currentTimeMillis();

            // 时钟回退，拒绝生成ID
            AssertUtil.isTrue(currentTs < lastTs,
                I18nUtil.get("snowflake.clock.rollback", lastTs, currentTs, lastTs - currentTs));

            // 同一毫秒内，序列号自增
            if (currentTs == lastTs) {
                seq = (seq + 1) & SEQUENCE_MASK;
                // 序列号耗尽，阻塞等待下一毫秒后重试
                if (seq == 0) {
                    waitNextMillis(lastTs);
                    continue;
                }
            } else {
                // 新毫秒，序列号重置为0
                seq = 0L;
            }

            // CAS更新，失败则重试
            long newPacked = (currentTs << SEQUENCE_BITS) | seq;
            if (tsSequence.compareAndSet(packed, newPacked)) {
                break;
            }
        }

        // 计算时间差值，第一层拦截溢出
        long tsDiff = currentTs - TWEPOCH;
        AssertUtil.isTrue(tsDiff > MAX_TIMESTAMP_DIFF,
            I18nUtil.get("snowflake.timestamp.overflow", MAX_TIMESTAMP_DIFF, tsDiff));

        // 拼装64位ID
        long id = (tsDiff << TIMESTAMP_LEFT_SHIFT) |
            (dataCenterId << DATA_CENTER_ID_SHIFT) |
            (workerId << WORKER_ID_SHIFT) | seq;

        // 兜底二次校验：强制保证ID为正数，杜绝极端溢出场景漏判
        AssertUtil.isTrue(id < 0, I18nUtil.get("snowflake.negative.id", tsDiff, id));

        return id;
    }

    /** 阻塞等待直到下一毫秒（仅在序列号耗尽时调用，极低频） */
    private void waitNextMillis(long lastTs) {
        long ts = System.currentTimeMillis();
        while (ts <= lastTs) {
            ts = System.currentTimeMillis();
        }
    }

    // ===================== 实现 IUuidGenerator 接口 =====================
    @Override
    public byte[] getUuid(int exceptLength) {
        return getUuidString(exceptLength).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getUuidString(int exceptLength) {
        StringBuilder builder = new StringBuilder();
        while (builder.length() < exceptLength) {
            long id = getUuidLong();
            builder.append(Long.toHexString(id));
        }
        return builder.substring(0, exceptLength);
    }

    @Override
    public Long getUuidLong() {
        return nextId();
    }
}