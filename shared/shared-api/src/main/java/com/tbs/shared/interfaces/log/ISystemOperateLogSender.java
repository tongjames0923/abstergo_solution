package com.tbs.shared.interfaces.log;

import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;

/**
 * 系统操作日志发送接口
 *
 * @author abstergo
 */
public interface ISystemOperateLogSender {
    /**
     * 日志
     *
     * @param operateRecord 操作记录
     */
    void log(SystemOperateRecordEntity operateRecord);

    /**
     * 刷新
     */
    default void flush() {
        return;
    }

}
