package com.tbs.common.delegate;

/**
 * 后台定时任务接口
 *
 * @author abstergo
 */
public interface IBackgroundJob {

    /**
     * 任务名称
     *
     * @return
     */
    public String name();

    /**
     * 任务描述
     *
     * @return
     */
    public String description();

    /**
     * 执行方法
     */
    void execute();
}
