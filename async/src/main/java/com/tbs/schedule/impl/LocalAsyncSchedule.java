package com.tbs.schedule.impl;

import com.tbs.schedule.ISchedule;
import com.tbs.schedule.IScheduleExecuteErrorHandle;
import com.tbs.schedule.IScheduleTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 本地异步定时计划器
 *
 * @author tongj
 */
@Slf4j
public class LocalAsyncSchedule implements ISchedule, InitializingBean, DisposableBean {

    /**
     * 执行器
     */
    private ExecutorService executorService;

    private DelayQueue<DelayedTaskWrapper> delayQueue = new DelayQueue<>();

    private int flag = 0;

    private IScheduleExecuteErrorHandle executeErrorHandle;

    public LocalAsyncSchedule(ExecutorService executorService, IScheduleExecuteErrorHandle executeErrorHandle) {
        this.executorService = executorService;
        this.executeErrorHandle = executeErrorHandle;
    }

    @Override
    public IScheduleTask pin(Runnable runnable, Duration delay) {
        Date shouldRunTime = new Date(System.currentTimeMillis() + delay.toMillis());
        SimpleScheduleTask scheduleTask = new SimpleScheduleTask(shouldRunTime, runnable, this);
        delayQueue.put(new DelayedTaskWrapper(scheduleTask));
        return scheduleTask;
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
        executorService.shutdown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        flag = 1;
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (flag == 1) {
                    DelayedTaskWrapper scheduleTask = null;
                    try {
                        scheduleTask = delayQueue.poll(5, TimeUnit.MINUTES);
                        if (scheduleTask == null) {
                            log.trace("存在空任务在队列中");
                            continue;
                        }
                        if (!scheduleTask.isPin()) {
                            log.debug("任务被提前取消 {}", scheduleTask.getRunnable().toString());
                            continue;
                        }
                        scheduleTask.getRunnable().run();
                        scheduleTask.unPin();
                    } catch (Exception e) {
                        if (getExecuteErrorHandle() == null) {
                            log.warn("schedule task error", e);
                            continue;
                        }
                        getExecuteErrorHandle().handle(scheduleTask, e, LocalAsyncSchedule.this);
                    }
                }
            }
        });
    }
}
