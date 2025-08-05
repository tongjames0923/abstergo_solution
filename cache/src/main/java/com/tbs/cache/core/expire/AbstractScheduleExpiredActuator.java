package com.tbs.cache.core.expire;

import com.tbs.common.error.AssertUtil;
import com.tbs.schedule.ISchedule;
import com.tbs.schedule.IScheduleTask;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Date;

/**
 * 抽象的缓存过期处理类
 *
 * @author tongj
 */
public abstract class AbstractScheduleExpiredActuator<R> implements IExpiredActuator<R> {

    /**
     * 缓存过期任务
     */
    @Data
    private static class ExpireTask<R> implements Runnable {
        private R cache;
        private Object key;
        private IExpireRemove<R> remove;

        public ExpireTask(R cache, Object key, IExpireRemove<R> remove) {
            AssertUtil.notNull(cache, "cache can not be null");
            AssertUtil.notNull(key, "key can not be null");
            AssertUtil.notNull(remove, "remove can not be null");
            this.cache = cache;
            this.key = key;
            this.remove = remove;
        }

        @Override
        public void run() {
            remove.remove(cache, key);
        }
    }

    public static interface IExpireRemove<R> {
        /**
         * 移除缓存
         *
         * @param cache
         * @param key
         */
        void remove(R cache, Object key);
    }

    /**
     * 获取移除缓存的函数
     *
     * @return
     */
    protected abstract IExpireRemove<R> getRemoveFunction();

    /**
     * 获取调度器
     *
     * @return
     */
    protected abstract ISchedule getSchedule();

    @Override
    public boolean expire(@NotNull R cache, @NotNull Object key, @NotNull Date expireTime) {
        long time = expireTime.getTime() - System.currentTimeMillis();
        if (time <= 0) {
            time = 1;
        }
        IScheduleTask task =
            getSchedule().pin(new ExpireTask(cache, key, getRemoveFunction()), Duration.ofMillis(time));
        AssertUtil.notNull(task, "计划任务创建失败");
        return task.isPin();
    }
}
