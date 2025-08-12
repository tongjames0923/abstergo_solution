package com.tbs.broadcast.idempotency;

import com.tbs.broadcast.publisher.IPublishResult;
import org.jetbrains.annotations.NotNull;

/**
 * 事件幂等性管理器
 *
 * @author abstergo
 */
public interface IEventIdempotencyManager {

    /**
     * 检查事件是否已经处理过
     *
     * @param publishResult
     * @return
     */
    @NotNull EventIdempotencyResult checkIdempotency(@NotNull IPublishResult publishResult);

    /**
     * 提交处理结果
     *
     * @param publishResult
     * @param result
     */
    void submitIdempotencyCheck(@NotNull IPublishResult publishResult, @NotNull EventIdempotencyResult result);

}
