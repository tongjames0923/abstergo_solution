package com.tbs.mq.api.retry;

import java.time.Duration;

/**
 * 重试延迟策略契约
 * <p>
 * 根据当前重试次数计算下一次重投递前应等待的时长。
 * 典型实现为指数退避（如30s→2min→10min）或固定档位数组。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public interface IRetryDelayPolicy {

    /**
     * 计算下一次重投递的延迟时长
     *
     * @param retryCount 当前已完成的重试次数（首次失败入库时为0）
     * @return 距离下次重投递的等待时长，不能为负
     */
    Duration nextDelay(int retryCount);

}
