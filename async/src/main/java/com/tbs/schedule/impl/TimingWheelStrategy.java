package com.tbs.schedule.impl;

import com.tbs.schedule.api.ScheduleStrategy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 时间轮调度策略
 * <p>
 * 纯数据结构实现，不关心线程协调和任务创建。基于环形数组 + round 计数的高效延迟调度算法。
 * 元素统一为 {@link ScheduleStrategy.Schedulable}，调度器内核内部 {@code ScheduledEntry} 实现该契约，
 * 不再持有反向引用或生命周期状态，消除旧实现双层 {@code isPin} 的脆弱耦合。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class TimingWheelStrategy implements ScheduleStrategy {

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
    public void insert(Schedulable task) {
        long rem = task.getRemain().toMillis();
        long delayMs = rem + System.currentTimeMillis() - beginTime;
        long perRound = (long) tickDuration * wheelSize;
        long round = delayMs / perRound;
        WheelTask wheelTask = new WheelTask(task, round);
        long tick = (currentTick + (delayMs / tickDuration)) % wheelSize;
        wheel[Math.toIntExact(tick)].add(wheelTask);
        log.debug("任务添加成功。槽位={}, 轮数={}", tick, round);
    }

    @Override
    public Collection<Schedulable> pollExpired() {
        Collection<Schedulable> result = new LinkedList<>();
        long currentTime = System.currentTimeMillis();
        int ticksAdvanced = 0;
        while (beginTime <= currentTime && ticksAdvanced < maxTicksPerPoll) {
            List<WheelTask> currentSlot = wheel[currentTick];
            var tasks = currentSlot.iterator();
            while (tasks.hasNext()) {
                WheelTask wheelTask = tasks.next();
                if (wheelTask.getRound() <= 0) {
                    result.add(wheelTask.original);
                    tasks.remove();
                } else {
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
    public Collection<Schedulable> dropAll() {
        List<Schedulable> tasks = new LinkedList<>();
        for (int i = 0; i < wheelSize; i++) {
            tasks.addAll(wheel[i].stream().map(w -> w.original).collect(Collectors.toList()));
            wheel[i].clear();
        }
        return tasks;
    }

    /**
     * 时间轮内部任务包装，附加 round 计数
     */
    static final class WheelTask {

        final Schedulable original;

        @Getter
        @Setter
        long round;

        WheelTask(Schedulable original, long round) {
            this.original = original;
            this.round = round;
        }
    }
}
