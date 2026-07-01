package com.tbs.schedule;

import com.tbs.async.annotation.LongTermAsync;
import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.lock.util.LockUtil;
import com.tbs.schedule.impl.SimpleScheduleTask;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 默认调度器实现
 * <p>
 * 将线程协调（锁、信号量）与调度策略（时间轮/DelayQueue）组合在一起， 替代原有的多层继承结构。
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public class DefaultSchedule implements ISchedule, IStartup {

    private volatile int flag = 0;

    private final IScheduleExecuteErrorHandle executeErrorHandle;

    @Getter
    private final long pollTime;

    @Getter
    private final TimeUnit pollTimeUnit;

    @Getter
    @Setter
    private boolean isExecuteAllOnDestroy = false;

    private final ScheduleStrategy<IScheduleTask> strategy;

    private final Semaphore semaphore = new Semaphore(0);

    /**
     * 构造调度器
     *
     * @param executeErrorHandle 错误处理器
     * @param pollTime           轮询等待时间
     * @param pollTimeUnit       轮询等待时间单位
     * @param strategy           调度策略（时间轮或DelayQueue）
     */
    public DefaultSchedule(IScheduleExecuteErrorHandle executeErrorHandle, long pollTime, TimeUnit pollTimeUnit,
        ScheduleStrategy<IScheduleTask> strategy) {
        this.executeErrorHandle = executeErrorHandle;
        this.pollTime = pollTime;
        this.pollTimeUnit = pollTimeUnit;
        this.strategy = strategy;
    }

    /**
     * 构造调度器，使用默认轮询参数（5秒）
     */
    public DefaultSchedule(IScheduleExecuteErrorHandle executeErrorHandle, ScheduleStrategy<IScheduleTask> strategy) {
        this(executeErrorHandle, 5L, TimeUnit.SECONDS, strategy);
    }

    @Override
    public IScheduleTask pin(Runnable runnable, Duration delay) {
        AssertUtil.notNull(runnable, "runnable can not be null");
        if (delay == null) {
            delay = Duration.ZERO;
        }
        IScheduleTask task =
            new SimpleScheduleTask(new Date(System.currentTimeMillis() + delay.toMillis()), runnable, this);
        LockUtil.getInstance().lockThen(this, () -> strategy.insert(task));
        semaphore.release();
        return task;
    }

    @Override
    public void unPin(IScheduleTask task) {
        task.unPin();
    }

    @Override
    public IScheduleExecuteErrorHandle getExecuteErrorHandle() {
        return executeErrorHandle;
    }

    @Override
    public void destroy() throws Exception {
        flag = 0;
        semaphore.release();
        if (isExecuteAllOnDestroy()) {
            log.warn("销毁计划器中...... 所有任务都将提前执行...");
            handleTaskList(strategy.dropAll());
        }
    }

    @Override
    @LongTermAsync(value = "本地异步定时器")
    public void startUp() throws RuntimeException {
        flag = 1;
        handle();
    }

    private void handle() {
        while (flag == 1) {
            Collection<IScheduleTask> tasks = pollTask();
            if (CollUtil.isEmpty(tasks)) {
                log.trace("存在空任务在队列中");
                continue;
            }
            handleTaskList(tasks);
        }
    }

    private Collection<IScheduleTask> pollTask() {
        log.trace("poll task Begin Wait");
        try {
            semaphore.tryAcquire(pollTime, pollTimeUnit);
        } catch (InterruptedException e) {
            log.warn("poll task error", e);
            Thread.currentThread().interrupt();
        }
        log.trace("poll task End Wait");
        final var holder = new Object() {
            Collection<IScheduleTask> tasks;
        };
        LockUtil.getInstance().lockThen(this, () -> holder.tasks = strategy.pollExpired());
        return holder.tasks;
    }

    private void handleTaskList(Collection<IScheduleTask> tasks) {
        for (IScheduleTask task : tasks) {
            try {
                task.getRunnable().run();
                task.unPin();
            } catch (Exception e) {
                if (getExecuteErrorHandle() == null) {
                    log.warn("schedule task error", e);
                    continue;
                }
                getExecuteErrorHandle().handle(task, e, this);
            }
        }
    }
}
