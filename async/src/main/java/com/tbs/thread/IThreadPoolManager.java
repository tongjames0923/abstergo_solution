package com.tbs.thread;

import org.jetbrains.annotations.NotNull;

/**
 * 线程池管理器
 *
 * @author tboss
 */
public interface IThreadPoolManager {
    /**
     * 提交任务
     *
     * @param request
     * @param onThreadError
     */
    void submit( IThreadRequest request, IOnThreadError onThreadError);
}
