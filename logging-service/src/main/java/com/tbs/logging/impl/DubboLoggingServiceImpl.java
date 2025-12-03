package com.tbs.logging.impl;

import com.tbs.logging.config.LoggingConfig;
import com.tbs.shared.interfaces.log.ISystemOperateLog;
import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@DubboService
public class DubboLoggingServiceImpl implements ISystemOperateLog {

    @Resource(name = LoggingConfig.SYSTEM_OPERATE_LOG_BEAN_NAME)
    ISystemOperateLog systemOperateLog;

    @Override
    public void log(SystemOperateRecordEntity operateRecord) {
        systemOperateLog.log(operateRecord);
    }

    @Override
    public void flush() {
        systemOperateLog.flush();
    }

    @Override
    public List<SystemOperateRecordEntity> listSystemOperateRecords(Date beginTime, Date endTime) {
        return systemOperateLog.listSystemOperateRecords(beginTime, endTime);
    }
}
