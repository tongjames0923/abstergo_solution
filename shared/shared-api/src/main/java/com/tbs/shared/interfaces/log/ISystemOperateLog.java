package com.tbs.shared.interfaces.log;

import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;

import java.util.Date;
import java.util.List;

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

    /**
     * 刷新
     */
    default void flush() {
        return;
    }

    /**
     * 查询系统操作记录
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 系统操作记录列表
     */
    default List<SystemOperateRecordEntity> listSystemOperateRecords(Date beginTime, Date endTime) {
        return null;
    }

}
