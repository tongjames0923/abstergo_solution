package com.tbs.thread;

import java.util.concurrent.Executor;

/**
 * 线程请求接口
 *
 * @author tongj
 */
public interface IThreadRequest {

    public static interface IThreadCall {
        /**
         * 任务
         *
         * @param request
         * @param threadPoolManager
         */
        void call(IThreadRequest request, IThreadPoolManager threadPoolManager);
    }

    /**
     * 优先级
     *
     * @return
     */
    int getPriority();

    /**
     * 预计耗时, 单位毫秒
     *
     * @return
     */
    int getExpectedCostTime();

    /**
     * 预计执行器名称
     *
     * @return
     */
    String getExpectedExecutorServiceName();

    /**
     * 获取请求任务名称
     *
     * @return
     */
    String getName();

    /**
     * 获取任务
     *
     * @return
     */
    IThreadCall getThreadCall();
}
