package com.tbs.cdc.source;

import com.tbs.cdc.model.CdcEvent;

import java.util.function.Consumer;

/**
 * 数据库无关的变更捕获源抽象
 * <p>
 * 定义CDC捕获能力的统一生命周期契约，MySQL/PostgreSQL/MongoDB等各提供一套实现子模块。
 * 实现方在{@code start}后持续读取数据库日志流，将变更转换为{@link CdcEvent}并通过
 * {@code handler}回调上报；{@code stop}须遵循优雅停止协议，保证已捕获事件的位点被提交，
 * 避免断点续传丢失消息。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface CdcSource {

    /**
     * 启动捕获，将变更事件通过处理器回调上报
     * <p>
     * 本方法应为非阻塞或自行管理后台线程，调用返回后即开始捕获；重复调用应被幂等处理。
     * </p>
     *
     * @param handler 变更事件处理器，捕获到事件时同步回调
     */
    void start(Consumer<CdcEvent> handler);

    /**
     * 停止捕获
     * <p>
     * 须优雅关闭底层引擎并提交消费位点，保证重启后可从上次位点续传。
     * </p>
     */
    void stop();

    /**
     * 获取捕获源类型标识
     *
     * @return 类型标识，如mysql/postgresql/mongodb
     */
    String getSourceType();

    /**
     * 判断捕获源是否处于运行状态
     *
     * @return true表示正在捕获，false表示未启动或已停止
     */
    boolean isRunning();
}
