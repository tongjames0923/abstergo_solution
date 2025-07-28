package com.tbs.thread;

import org.jetbrains.annotations.NotNull;

/**
 * 线程池管理器
 *
 * @author tboss
 */
public interface IThreadPoolManager {
    void submit(@NotNull IThreadRequest request, IOnThreadError onThreadError);
}
