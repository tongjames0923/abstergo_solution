package com.tbs.schedule;

import com.tbs.common.data.IFeatureWrapper;
import com.tbs.common.error.AssertUtil;
import com.tbs.lock.util.LockUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 基于时间轮算法的异步后台调度器
 *
 * @author abstergo
 */
@Slf4j
public abstract class AbstractTimingWheelAsyncBackgroundSchedule<TASK extends IScheduleTask>
    extends AbstractAsyncBackgroundSchedule<TASK> {
    Semaphore semaphore = new Semaphore(0);
    // 时间轮相关参数
    private final long tickDuration; // 每个槽位的时间间隔
    private final int wheelSize; // 时间轮大小(槽数量)

    // 时间轮核心结构
    private final List<TimingWheelTask<TASK>>[] wheel; // 时间轮数组
    private volatile long beginTime; // 当前时间(毫秒)
    private int currentTick = 0; // 当前指针位置

    /**
     * 构造函数，初始化时间轮调度器
     *
     * @param executeErrorHandle 错误处理器
     * @param pollTime           轮询时间
     * @param pollTimeUnit       轮询时间单位
     * @param tickDuration       时间轮每个槽位的时间间隔
     * @param wheelSize          时间轮槽数量
     * @param timeUnit           时间单位
     */
    public AbstractTimingWheelAsyncBackgroundSchedule(IScheduleExecuteErrorHandle executeErrorHandle, Long pollTime,
        TimeUnit pollTimeUnit, long tickDuration, int wheelSize, TimeUnit timeUnit) {
        super(executeErrorHandle, pollTime, pollTimeUnit);
        this.tickDuration = timeUnit == null ? tickDuration : timeUnit.toMillis(tickDuration);
        this.wheelSize = wheelSize;
        this.beginTime = System.currentTimeMillis();
        log.debug("初始化时间轮 {} {} {} {} {} {}", tickDuration, wheelSize, timeUnit, pollTime, pollTimeUnit,
            beginTime);
        // 初始化时间轮
        this.wheel = new List[wheelSize];
        for (int i = 0; i < wheelSize; i++) {
            this.wheel[i] = new LinkedList<>();
        }
    }

    /**
     * 提交任务到时间轮中
     *
     * @param task 待提交的任务
     */
    @Override
    protected void submitTask(TASK task) {
        LockUtil.getInstance().lockThen(this, () -> {
            long rem = task.getRemain().toMillis();
            long delayMs = rem + System.currentTimeMillis() - beginTime;
            log.debug("所需延迟 {} 校正后延迟 {}", task.getRemain(), delayMs);
            long perRound = tickDuration * wheelSize;
            // 计算任务在时间轮中的位置
            long round = delayMs / perRound;
            TimingWheelTask<TASK> wheelTask = new TimingWheelTask<>(task, round);
            long tick = (currentTick + (delayMs / tickDuration)) % wheelSize;
            this.wheel[Math.toIntExact(tick)].add(wheelTask);
            log.debug("任务添加成功。槽位{} ,轮数:{} ,基准时间 {}", tick, round, new Date(beginTime));
        });
    }

    /**
     * 轮询到期的任务
     *
     * @param t    轮询时间
     * @param unit 时间单位
     * @return 到期的任务集合
     */
    @Override
    protected Collection<TASK> pollTask(long t, TimeUnit unit) {
        Collection<TASK> result = new LinkedList<>();
        boolean acq = false;
        log.trace("poll task Begin Wait");
        try {
            acq = semaphore.tryAcquire(t, unit);
        } catch (InterruptedException e) {
            log.warn("poll task error", e);
        }
        log.trace("poll task End Wait {}", acq);
        LockUtil.getInstance().lockThen(this, () -> {
            //处理时间轮
            long currentTime = System.currentTimeMillis();
            // 更新时间轮指针位置
            while (beginTime <= currentTime) {
                // 获取当前槽位中的任务
                List<TimingWheelTask<TASK>> currentSlot = wheel[currentTick];
                var tasks = currentSlot.iterator();
                while (tasks.hasNext()) {
                    TimingWheelTask<TASK> wheelTask = tasks.next();
                    if (wheelTask.getRound() <= 0) {
                        log.debug("任务已到期 {}", wheelTask);
                        // 任务已到期，添加到结果中
                        result.add(wheelTask.getOrginal());
                        // 从槽位中移除任务
                        tasks.remove();
                    } else {
                        // 减少任务圈数
                        log.debug("任务未到期 {}", wheelTask);
                        wheelTask.setRound(wheelTask.getRound() - 1);
                    }
                }
                // 更新时间轮状态
                currentTick = (currentTick + 1) % wheelSize;
                beginTime += tickDuration;
                currentTime = System.currentTimeMillis();
            }

        });
        return result;
    }

    /**
     * 创建任务实例
     *
     * @param runnable 任务执行体
     * @param delay    延迟时间
     * @return 任务实例
     */
    @Override
    protected TASK createTask(Runnable runnable, Duration delay) {
        AssertUtil.notNull(runnable, "runnable can not be null");
        if (delay == null) {
            delay = Duration.ZERO;
        }
        return createTimingWheelTask(runnable, delay);
    }

    /**
     * 创建时间轮任务
     *
     * @param runnable 任务执行体
     * @param delay    延迟时间
     * @return 时间轮任务
     */
    protected abstract TASK createTimingWheelTask(Runnable runnable, Duration delay);

    @ToString
    private static final class TimingWheelTask<TASK extends IScheduleTask>
        implements IScheduleTask, IFeatureWrapper<TASK> {
        private TASK orginal;
        @Getter
        @Setter
        private long round;

        public TimingWheelTask(TASK orginal, long round) {
            this.orginal = orginal;
            this.round = round;
        }

        @Override
        public @NotNull Duration getRemain() {
            return orginal.getRemain();
        }

        @Override
        public @NotNull Runnable getRunnable() {
            return orginal.getRunnable();
        }

        @Override
        public @NotNull ISchedule getSchedule() {
            return orginal.getSchedule();
        }

        @Override
        public boolean isPin() {
            return orginal.isPin();
        }

        @Override
        public void unPin() {
            orginal.unPin();
        }

        @Override
        public TASK getOrginal() {
            return orginal;
        }

        @Override
        public void setOrginal(TASK orginal) {
            this.orginal = orginal;
        }
    }
}
