package com.tbs.shared.interfaces.log.impl;

import com.tbs.shared.interfaces.log.ISystemOperateLogSender;
import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleLoggerSender implements ISystemOperateLogSender {

    private static final Logger logger = LoggerFactory.getLogger(SimpleLoggerSender.class);

    @Override
    public void log(SystemOperateRecordEntity operateRecord) {
        logger.info("系统运行日志：{}", operateRecord);
    }

    @Override
    public void flush() {
        ISystemOperateLogSender.super.flush();
    }
}
