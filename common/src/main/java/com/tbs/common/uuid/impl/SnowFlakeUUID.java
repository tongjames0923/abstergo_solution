package com.tbs.common.uuid.impl;

import com.tbs.common.util.AssertUtil;
import com.tbs.common.uuid.IUuidGenerator;

import java.nio.charset.StandardCharsets;

/**
 * @author Abstergo
 */
public class SnowFlakeUUID implements IUuidGenerator {
    // ==============================Fields===========================================
    /** 开始时间截 (2020-08-28) */
    private final long twepoch = 1598598185157L;

    /** 机器id所占的位数 */
    private final long workerIdBits = 5L;

    /** 数据标识id所占的位数 */
    private final long datacenterIdBits = 5L;

    /** 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数) */
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    /** 支持的最大数据标识id，结果是31 */
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    /** 序列在id中占的位数 */
    private final long sequenceBits = 12L;

    /** 机器ID向左移12位 */
    private final long workerIdShift = sequenceBits;

    /** 数据标识id向左移17位(12+5) */
    private final long datacenterIdShift = sequenceBits + workerIdBits;

    /** 时间截向左移22位(5+5+12) */
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    /** 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095) */
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    /** 工作机器ID(0~31) */
    private long workerId;

    /** 数据中心ID(0~31) */
    private long datacenterId;

    /** 毫秒内序列(0~4095) */
    private long sequence = 0L;

    /** 上次生成ID的时间截 */
    private long lastTimestamp = -1L;

    //==============================Constructors=====================================

    /**
     * 构造函数
     *
     * @param workerId     工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31) 此方法是判断传入的机房号和机器号是否超过了最大值，即31，或者小于0
     */
    public SnowFlakeUUID(long workerId, long datacenterId) {
        AssertUtil.isTrue(workerId <= maxWorkerId && workerId >= 0, "worker Id can t be greater than %d or less than 0",
            maxWorkerId);
        AssertUtil.isTrue(datacenterId <= maxDatacenterId && datacenterId >= 0,
            "datacenter Id can t be greater than %d or less than 0", maxDatacenterId);
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public SnowFlakeUUID() {
        this(0, 0);
    }

    // ==============================Methods==========================================
    /*
     * 核心方法
     * 获得下一个ID (该方法是线程安全的)
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        //1.获取当前的系统时间
        long timestamp = timeGen();

        //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
        AssertUtil.isTrue(timestamp > lastTimestamp,
            "Clock moved backwards.  Refusing to generate id for {} milliseconds", timestamp - lastTimestamp);

        //如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            // sequence 要增1, 但要预防sequence超过 最大值4095，所以要 与 SEQUENCE_MASK 按位求与
            // 即如果此时sequence等于4095，加1后为4096，再和4095按位与后，结果为0
            sequence = (sequence + 1) & sequenceMask;
            // 毫秒内序列溢出
            if (sequence == 0) {
                //阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        //时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }

        //上次生成ID的时间截
        //把当前时间赋值给 lastTime, 以便下一次判断是否处在同一个毫秒内
        lastTimestamp = timestamp;

        //移位并通过或运算拼到一起组成64位的ID
        long id = ((timestamp - twepoch) << timestampLeftShift) // 时间戳减去默认时间 再左移22位 与运算
            | (datacenterId << datacenterIdShift) // 机房号 左移17位 与运算
            | (workerId << workerIdShift) // 机器号 左移12位 与运算
            | sequence; // 序列号无需左移 直接进行与运算
        return id;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }

    @Override
    public byte[] getUuid(int exceptLength) {
        return getUuidString(exceptLength).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getUuidString(int exceptLength) {
        String uuid = String.valueOf(nextId());
        while (uuid.length() < exceptLength) {
            uuid += String.valueOf(nextId());
        }
        return uuid.substring(0, exceptLength);
    }
}
