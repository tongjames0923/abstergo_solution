package com.tbs.shared.interfaces.log;

import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;

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
