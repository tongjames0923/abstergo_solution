package com.tbs.app.log;

import com.tbs.app.pojo.entity.SystemOperateRecordEntity;

/**
 * 系统操作日志
 *
 * @author jiangnan
 * @date 2020/01/09
 */
public interface ISystemOperateLog {
    /**
     * 日志
     *
     * @param operateRecord 操作记录
     */
    void log(SystemOperateRecordEntity operateRecord);
}
