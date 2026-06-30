package com.tbs.schedule.impl;

import com.tbs.common.data.IFeatureWrapper;
import com.tbs.schedule.IScheduleTask;
import com.tbs.schedule.ScheduleStrategy;
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
import java.util.stream.Collectors;

/**
 * 时间轮调度策略
 * <p>
 * 纯数据结构实现，不关心线程协调和任务创建。
 * 基于环形数组 + round 计数的高效延迟调度算法。
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public class TimingWheelStrategy implements ScheduleStrategy<IScheduleTask> {

    private final long tickDuration;
    private final int wheelSize;
    private final int maxTicksPerPoll;

    @SuppressWarnings("unchecked")
    private final List<WheelTask>[] wheel;
    private long beginTime;
    private int currentTick = 0;

    /**
     * 构造时间轮策略
     *
     * @param tickDuration    每个槽位的时间间隔（毫秒）
     * @param wheelSize       时间轮槽数量
     * @param maxTicksPerPoll 单次轮询最大推进槽数
     */
    public TimingWheelStrategy(long tickDuration, int wheelSize, int maxTicksPerPoll) {
        this.tickDuration = tickDuration;
        this.wheelSize = wheelSize;
        this.maxTicksPerPoll = maxTicksPerPoll;
        this.beginTime = System.currentTimeMillis();
        this.wheel = new List[wheelSize];
        for (int i = 0; i < wheelSize; i++) {
            this.wheel[i] = new LinkedList<>();
        }
        log.debug("初始化时间轮 tickDuration={}ms, wheelSize={}, maxTicksPerPoll={}", tickDuration, wheelSize,
            maxTicksPerPoll);
    }

    @Override
    public void insert(IScheduleTask task) {
        long rem = task.getRemain().toMillis();
        long delayMs = rem + System.currentTimeMillis() - beginTime;
        long perRound = tickDuration * wheelSize;
        long round = delayMs / perRound;
        WheelTask wheelTask = new WheelTask(task, round);
        long tick = (currentTick + (delayMs / tickDuration)) % wheelSize;
        wheel[Math.toIntExact(tick)].add(wheelTask);
        log.debug("任务添加成功。槽位={}, 轮数={}, 基准时间={}", tick, round, new Date(beginTime));
    }

    @Override
    public Collection<IScheduleTask> pollExpired() {
        Collection<IScheduleTask> result = new LinkedList<>();
        long currentTime = System.currentTimeMillis();
        int ticksAdvanced = 0;

        while (beginTime <= currentTime && ticksAdvanced < maxTicksPerPoll) {
            List<WheelTask> currentSlot = wheel[currentTick];
            var tasks = currentSlot.iterator();
            while (tasks.hasNext()) {
                WheelTask wheelTask = tasks.next();
                if (!wheelTask.isPin()) {
                    log.debug("任务已取消，移除 {}", wheelTask);
                    tasks.remove();
                } else if (wheelTask.getRound() <= 0) {
                    log.debug("任务已到期 {}", wheelTask);
                    result.add(wheelTask.orginal);
                    tasks.remove();
                } else {
                    log.debug("任务未到期 {}", wheelTask);
                    wheelTask.setRound(wheelTask.getRound() - 1);
                }
            }
            currentTick = (currentTick + 1) % wheelSize;
            beginTime += tickDuration;
            ticksAdvanced++;
        }
        if (ticksAdvanced >= maxTicksPerPoll) {
            log.warn("单次 poll 推进已达上限 {} 槽位，剩余未处理", maxTicksPerPoll);
        }
        return result;
    }

    @Override
    public Collection<IScheduleTask> dropAll() {
        List<IScheduleTask> tasks = new LinkedList<>();
        for (int i = 0; i < wheelSize; i++) {
            tasks.addAll(wheel[i].stream().filter(WheelTask::isPin).map(w -> w.orginal)
                .collect(Collectors.toList()));
            wheel[i].clear();
        }
        return tasks;
    }

    /**
     * 时间轮内部任务包装，附加 round 计数
     */
    @ToString
    static final class WheelTask implements IScheduleTask, IFeatureWrapper<IScheduleTask> {
        IScheduleTask orginal;
        @Getter
        @Setter
        long round;

        WheelTask(IScheduleTask orginal, long round) {
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
        public @NotNull com.tbs.schedule.ISchedule getSchedule() {
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
        public IScheduleTask getOrginal() {
            return orginal;
        }

        @Override
        public void setOrginal(IScheduleTask orginal) {
            this.orginal = orginal;
        }
    }
}
