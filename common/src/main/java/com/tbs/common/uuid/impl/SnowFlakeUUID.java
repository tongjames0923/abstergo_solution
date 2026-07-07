package com.tbs.common.uuid.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.uuid.IUuidGenerator;

import java.nio.charset.StandardCharsets;

/**
 * 雪花算法ID生成器 64位long结构： 1bit 符号位(恒0) | 41bit 时间戳差值 | 5bit 数据中心ID | 5bit 机器ID | 12bit 序列号 基准时间：2020-08-28 1598598185157
 * 最大支持时间差值：2199023255551，正常可使用至2140年左右 负数ID双重拦截：时间差值超限拦截 + 最终ID负数拦截
 *
 * @author Abstergo
 */
public class SnowFlakeUUID implements IUuidGenerator {
    // ===================== 时间基准 =====================
    // 2000-01-01 基准，可用到2090年左右
    private static final long TWEPOCH = 946656000000L;

    // ===================== 位分配长度 =====================
    /** 机器id位数 */
    private static final long WORKER_ID_BITS = 5L;
    /** 数据中心id位数 */
    private static final long DATA_CENTER_ID_BITS = 5L;
    /** 毫秒内序列号位数 */
    private static final long SEQUENCE_BITS = 12L;

    // ===================== 各段最大值掩码 =====================
    /** 机器ID最大值 0~31 */
    private static final long MAX_WORKER_ID = (1L << WORKER_ID_BITS) - 1;
    /** 数据中心ID最大值 0~31 */
    private static final long MAX_DATA_CENTER_ID = (1L << DATA_CENTER_ID_BITS) - 1;
    /** 毫秒内序列号最大值 0~4095 */
    private static final long SEQUENCE_MASK = (1L << SEQUENCE_BITS) - 1;
    /** 41位时间戳最大差值，超过会溢出产生负数ID */
    private static final long MAX_TIMESTAMP_DIFF = (1L << 41) - 1;

    // ===================== 左移位数 =====================
    /** 机器ID左移位数：12 */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    /** 数据中心ID左移位数：12+5=17 */
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    /** 时间戳左移位数：12+5+5=22 */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;

    // ===================== 运行时变量 =====================
    /** 工作机器ID 0~31 */
    private final long workerId;
    /** 数据中心ID 0~31 */
    private final long dataCenterId;
    /** 毫秒内序列 0~4095 */
    private long sequence = 0L;
    /** 上一次生成ID的时间戳 */
    private long lastTimestamp = -1L;

    //==============================构造器=====================================

    /**
     * @param workerId     机器ID [0,31]
     * @param dataCenterId 数据中心ID [0,31]
     */
    public SnowFlakeUUID(long workerId, long dataCenterId) {
        // AssertUtil.isTrue：条件为true时抛出异常（项目自定义语义）
        AssertUtil.isTrue(workerId < 0 || workerId > MAX_WORKER_ID, "SnowFlake 机器ID非法，范围0~{}，当前值：{}",
            MAX_WORKER_ID, workerId);
        AssertUtil.isTrue(dataCenterId < 0 || dataCenterId > MAX_DATA_CENTER_ID,
            "SnowFlake 数据中心ID非法，范围0~{}，当前值：{}", MAX_DATA_CENTER_ID, dataCenterId);
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    public SnowFlakeUUID() {
        this(0, 0);
    }

    // ===================== 核心生成方法 =====================

    /**
     * 线程安全生成唯一雪花ID
     *
     * @return 正数long ID，溢出直接抛异常，不会返回负数
     */
    public synchronized long nextId() {
        long currentTs = System.currentTimeMillis();

        // 时钟回退，拒绝生成ID
        AssertUtil.isTrue(currentTs < lastTimestamp,
            "SnowFlake 系统时钟回退，禁止生成ID！上次时间戳：{}，当前时间戳：{}，差值：{}ms", lastTimestamp, currentTs,
            lastTimestamp - currentTs);

        // 同一毫秒内，序列号自增
        if (currentTs == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 序列号耗尽，阻塞等待下一毫秒
            if (sequence == 0) {
                currentTs = waitNextMillis(lastTimestamp);
            }
        } else {
            // 新毫秒，序列号重置为0
            sequence = 0L;
        }

        // 计算时间差值，第一层拦截溢出
        long tsDiff = currentTs - TWEPOCH;
        AssertUtil.isTrue(tsDiff > MAX_TIMESTAMP_DIFF,
            "SnowFlake 时间戳差值溢出！最大允许差值：{}，当前差值：{}，请检查服务器系统时间是否跳至未来", MAX_TIMESTAMP_DIFF,
            tsDiff);

        // 拼装64位ID
        long id = (tsDiff << TIMESTAMP_LEFT_SHIFT) |
            (dataCenterId << DATA_CENTER_ID_SHIFT) |
            (workerId << WORKER_ID_SHIFT) |
            sequence;

        // 兜底二次校验：强制保证ID为正数，杜绝极端溢出场景漏判
        AssertUtil.isTrue(id < 0, "SnowFlake 生成负数ID，时间戳溢出！tsDiff={}, id={}", tsDiff, id);

        lastTimestamp = currentTs;
        return id;
    }

    /** 阻塞等待直到下一毫秒 */
    private long waitNextMillis(long lastTs) {
        long currentTs = System.currentTimeMillis();
        while (currentTs <= lastTs) {
            currentTs = System.currentTimeMillis();
        }
        return currentTs;
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
