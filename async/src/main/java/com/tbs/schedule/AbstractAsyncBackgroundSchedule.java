package com.tbs.schedule;

import com.tbs.async.annotation.LongTermAsync;
import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.CollUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @author abstergo
 */

@Slf4j
public abstract class AbstractAsyncBackgroundSchedule<TASK extends IScheduleTask> implements ISchedule, IStartup {

    private int flag = 0;

    private IScheduleExecuteErrorHandle executeErrorHandle;
    @Getter
    private long pollTime;
    @Getter
    private TimeUnit pollTimeUnit;

    @Getter
    @Setter
    private boolean isExecuteAllOnDestroy = false;

    public AbstractAsyncBackgroundSchedule(IScheduleExecuteErrorHandle executeErrorHandle, Long pollTime,
        TimeUnit pollTimeUnit) {
        this.executeErrorHandle = executeErrorHandle;
        if (pollTime == null || pollTimeUnit == null) {
            this.pollTime = 5;
            this.pollTimeUnit = TimeUnit.MINUTES;
        } else {
            this.pollTime = pollTime;
            this.pollTimeUnit = pollTimeUnit;
        }
    }

    /**
     * 提交任务
     *
     * @param task
     */
    protected abstract void submitTask(TASK task);

    /**
     * 创建任务
     *
     * @param runnable
     * @param delay
     * @return
     */
    protected abstract TASK createTask(Runnable runnable, Duration delay);

    /**
     * 获取任务
     *
     * @param t
     * @param unit
     * @return
     */
    protected abstract Collection<TASK> pollTask(long t, TimeUnit unit);

    /**
     * 丢弃所有任务
     *
     * @return
     */
    protected abstract Collection<TASK> dropAllTaskForClean();

    @Override
    public IScheduleTask pin(Runnable runnable, Duration delay) {
        TASK task = createTask(runnable, delay);
        AssertUtil.notNull(task, "task can not be null");
        submitTask(task);
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
        if (isExecuteAllOnDestroy()) {
            log.warn("销毁计划器中...... 所有任务都将提前执行...");
            handleTaskList(dropAllTaskForClean());
        }
    }

    private void handleTaskList(Collection<TASK> t) {
        for (TASK task : t) {
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

    protected void handle() {
        while (flag == 1) {
            Collection<TASK> tasks = null;
            tasks = pollTask(pollTime, pollTimeUnit);
            if (CollUtil.isEmpty(tasks)) {
                log.trace("存在空任务在队列中");
                continue;
            }
            handleTaskList(tasks);
        }
    }

    @Override
    @LongTermAsync(value = "本地异步定时器")
    public void startUp() throws RuntimeException {
        flag = 1;
        handle();
    }
}
