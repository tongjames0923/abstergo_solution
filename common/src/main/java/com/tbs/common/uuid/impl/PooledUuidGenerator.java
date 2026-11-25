package com.tbs.common.uuid.impl;

import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.uuid.IUuidGenerator;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * UUID 生成器池化包装器（双缓冲 + 异步预填充 + 耗尽降级）。
 *
 * <p>包装任意 {@link IUuidGenerator} 实现（雪花、随机数等）：后台批量调用
 * {@link IUuidGenerator#getUuidLong()} 预生成 ID 放入缓冲区，业务线程只做 “索引自增 + 数组读取”，把底层生成器的 CAS 自旋、时间查询或随机数计算摊薄。</p>
 *
 * <p><b>时间语义：</b>缓冲中的 ID 代表预生成时刻而非业务发放时刻。若底层算法把时间戳编入 ID
 * （如 {@link SnowFlakeUUID}），时间偏移取决于缓冲容量与每秒序号容量；业务强依赖 ID 时间戳 精确等于业务发生时间时，应减小缓冲或直接使用底层生成器。</p>
 *
 * <p><b>不解决：</b>WorkerId 唯一性、时钟回拨、随机质量等，均由被包装的生成器负责。</p>
 *
 * @author abstergo
 */
public class PooledUuidGenerator implements IUuidGenerator, AutoCloseable {

    /** 默认缓冲容量：按雪花算法 1ms 的序列号容量估算（4096） */
    private static final int DEFAULT_BUFFER_SIZE = 2048;

    /** 默认低水位比例：缓冲区剩余 1/5 时触发填充 */
    private static final int DEFAULT_REFILL_RATIO = 5;

    /** 被包装的底层生成器 */
    private final IUuidGenerator delegate;
    /** 单个缓冲区的 ID 数量 */
    private final int bufferSize;
    /** 低水位阈值，剩余量低于该值时触发异步填充 */
    private final int refillThreshold;
    /** 备用缓冲，填充完成后由切换线程 getAndSet(null) 取走 */
    private final AtomicReference<Buffer> next = new AtomicReference<>();
    /** 防止重复提交填充任务 */
    private final AtomicBoolean filling = new AtomicBoolean(false);
    /** 缓冲切换锁，仅在 current 耗尽时竞争，频率极低 */
    private final ReentrantLock switchLock = new ReentrantLock();
    /** 单线程填充器，串行批量生成，避免并发填充 */
    private final ExecutorService fillExecutor;
    /** 降级次数：缓冲未就绪时直接调用底层生成器的次数 */
    private final AtomicLong fallbackCount = new AtomicLong();
    /** 填充异常次数：底层生成器抛异常时增长 */
    private final AtomicLong fillErrorCount = new AtomicLong();
    /** 当前消费缓冲，volatile 保证切换后业务线程立即可见 */
    private volatile Buffer current;
    /** 关闭标记，关闭后不再提交新的填充任务 */
    private volatile boolean running = true;

    /**
     * 使用默认缓冲容量构造池化包装器
     *
     * @param delegate 任意 UUID 生成器实现
     */
    public PooledUuidGenerator(IUuidGenerator delegate) {
        this(delegate, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 使用指定缓冲容量构造池化包装器，低水位阈值取容量的 1/5
     *
     * @param delegate   任意 UUID 生成器实现
     * @param bufferSize 缓冲容量，建议 2048~8192
     */
    public PooledUuidGenerator(IUuidGenerator delegate, int bufferSize) {
        this(delegate, bufferSize, Math.max(1, bufferSize / DEFAULT_REFILL_RATIO));
    }

    /**
     * 构造池化包装器
     *
     * @param delegate        任意 UUID 生成器实现
     * @param bufferSize      缓冲容量，建议 2048~8192
     * @param refillThreshold 低水位阈值，剩余量低于该值时触发填充，建议取容量的 20%~50%
     */
    public PooledUuidGenerator(IUuidGenerator delegate, int bufferSize, int refillThreshold) {
        AssertUtil.isTrue(delegate == null).i18n(CommonI18nConstant.UUID_DELEGATE_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        AssertUtil.isTrue(bufferSize <= 0).i18n(CommonI18nConstant.UUID_BUFFER_SIZE_INVALID, bufferSize)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        AssertUtil.isTrue(refillThreshold <= 0 || refillThreshold >= bufferSize)
            .i18n(CommonI18nConstant.UUID_REFILL_THRESHOLD_INVALID, bufferSize, refillThreshold)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        this.delegate = delegate;
        this.bufferSize = bufferSize;
        this.refillThreshold = refillThreshold;

        /* 构造期同步预填充一次，耗时约为底层生成器产出 bufferSize 个 ID 的时间 */
        this.current = new Buffer(fillBuffer(bufferSize));

        this.fillExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "uuid-generator-pool-filler");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 批量预生成 ID
     *
     * @param size 预生成数量
     * @return 预生成的 ID 数组
     */
    private long[] fillBuffer(int size) {
        long[] buf = new long[size];
        for (int i = 0; i < size; i++) {
            buf[i] = delegate.getUuidLong();
        }
        return buf;
    }

    /**
     * 快路径低水位检查：仅在 pos 进入低水位区时才做后续判断
     *
     * @param pos 本次消费的缓冲下标
     */
    private void checkAndTriggerFill(int pos) {
        if (pos < bufferSize - refillThreshold) {
            return;
        }
        triggerFill();
    }

    /**
     * 提交一次异步填充（幂等：next 已有值或正在填充时直接返回）
     */
    private void triggerFill() {
        if (!running) {
            return;
        }
        if (next.get() != null) {
            return;
        }
        if (!filling.compareAndSet(false, true)) {
            return;
        }
        try {
            fillExecutor.execute(() -> {
                try {
                    long[] data = fillBuffer(bufferSize);
                    next.set(new Buffer(data));
                } catch (Throwable t) {
                    /* 底层生成器异常（如时钟回拨）：计数后等待下一次触发重试 */
                    fillErrorCount.incrementAndGet();
                } finally {
                    filling.set(false);
                }
            });
        } catch (RejectedExecutionException e) {
            filling.set(false);
        }
    }

    /**
     * 从缓冲中取下一个 ID，缓冲耗尽时走慢路径
     *
     * @return 下一个 ID
     */
    public long nextId() {
        Buffer buf = current;
        int pos = buf.pos.getAndIncrement();
        if (pos < bufferSize) {
            checkAndTriggerFill(pos);
            return buf.data[pos];
        }
        return slowPath();
    }

    /**
     * 当前缓冲耗尽后的慢路径：加锁切换备用缓冲，无备用则降级。 降级调用放在锁外，避免底层生成器阻塞时持有 switchLock。
     *
     * @return 下一个 ID
     */
    private long slowPath() {
        switchLock.lock();
        try {
            /* 重新读 current：可能已被其他线程切换 */
            Buffer buf = current;
            int pos = buf.pos.getAndIncrement();
            if (pos < bufferSize) {
                return buf.data[pos];
            }
            Buffer nextBuf = next.getAndSet(null);
            if (nextBuf != null) {
                current = nextBuf;
                int actual = nextBuf.pos.getAndIncrement();
                return nextBuf.data[actual];
            }
            /* 备用缓冲未就绪，触发异步填充，随后降级 */
            triggerFill();
        } finally {
            switchLock.unlock();
        }
        fallbackCount.incrementAndGet();
        return delegate.getUuidLong();
    }

    @Override
    public Long getUuidLong() {
        return nextId();
    }

    @Override
    public byte[] getUuid(int exceptLength) {
        return getUuidString(exceptLength).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getUuidString(int exceptLength) {
        StringBuilder builder = new StringBuilder();
        while (builder.length() < exceptLength) {
            builder.append(Long.toHexString(getUuidLong()));
        }
        return builder.substring(0, exceptLength);
    }

    /**
     * 关闭填充线程，建议由 Spring destroyMethod 或应用关闭钩子调用
     */
    @Override
    public void close() {
        running = false;
        fillExecutor.shutdown();
        try {
            if (!fillExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                fillExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            fillExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 降级次数：持续增长说明缓冲容量过小或填充过慢
     *
     * @return 降级次数
     */
    public long getFallbackCount() {
        return fallbackCount.get();
    }

    /**
     * 填充异常次数：底层生成器异常时增长
     *
     * @return 填充异常次数
     */
    public long getFillErrorCount() {
        return fillErrorCount.get();
    }

    /**
     * 获取缓冲容量
     *
     * @return 缓冲容量
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * 获取低水位阈值
     *
     * @return 低水位阈值
     */
    public int getRefillThreshold() {
        return refillThreshold;
    }

    /**
     * 缓冲单元：把数据数组和消费索引绑定，切换时整体替换，杜绝“新数组 + 旧索引”竞态
     */
    private static final class Buffer {
        final long[] data;
        final AtomicInteger pos = new AtomicInteger(0);

        Buffer(long[] data) {
            this.data = data;
        }
    }
}
