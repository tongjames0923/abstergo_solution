package com.tbs.logging.impl;

import com.tbs.common.util.base.CollUtil;
import com.tbs.logging.core.ILogExtractor;
import com.tbs.logging.form.LoggingRecordExtractForm;
import com.tbs.logging.vo.LogRecordVO;
import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;
import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.service.QueryEngine;
import com.tbs.trace.annotation.Traced;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DbLogExtractor implements ILogExtractor {

    @Traced
    @Override
    public List<LogRecordVO> extract(LoggingRecordExtractForm form) {
        if (form == null) {
            return Arrays.asList();
        }
        var q = QueryEngine.getEntityQueryWrapper(new SystemOperateRecordEntity()).query().where();
        if (CollUtil.isNotEmpty(form.getOperateTypes())) {
            q.in(SystemOperateRecordEntity::getOperateType, form.getOperateTypes());
        }
        if (form.getOperateTimeBegin() != null) {
            q.greaterThanOrEqual(SystemOperateRecordEntity::getOperateTime, form.getOperateTimeBegin());
        }
        if (form.getOperateTimeEnd() != null) {
            q.lessThanOrEqual(SystemOperateRecordEntity::getOperateTime, form.getOperateTimeEnd());
        }
        q.pour().getOrderSupport().ifPresent((c) -> {
            c.orderBy(SystemOperateRecordEntity::getOperateTime, OrderEnum.DESC);
        });
        return q.pour().fetch().stream().map(
            entity -> new LogRecordVO(entity.getOperateType(), entity.getOperateContent(), entity.getOperateUserName(),
                entity.getOperateIp(), entity.getOperateTime(), entity.getOperateResultJson(),
                entity.getOperateParamJson(), entity.getOperateError(), entity.getOperateErrorLevel(),
                new Date(entity.getOperateTime().getTime() + entity.getCostTime()), entity.getOperateUrl(),
                entity.getMethod())).collect(Collectors.toList());
    }
}
