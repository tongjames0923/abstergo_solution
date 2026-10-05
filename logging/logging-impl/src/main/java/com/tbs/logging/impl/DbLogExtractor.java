package com.tbs.logging.impl;

import cn.hutool.core.date.DateUtil;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.util.base.CollUtil;
import com.tbs.logging.core.ILogExtractor;
import com.tbs.logging.form.LoggingRecordExtractForm;
import com.tbs.logging.vo.LogRecordVO;
import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;
import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.service.QueryEngine;
import org.springframework.cache.annotation.Cacheable;

import static com.tbs.logging.impl.CacheNamesConstants.ALL_OPERATE_TYPES;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DbLogExtractor implements ILogExtractor {


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

        if (form.getPageNum() != null && form.getPageNum() >= 1) {
            var ref = new Object() {
                Integer size = form.getPageSize();
            };
            if (ref.size == null || ref.size <= 0) {
                ref.size = 15;
            }
            q.pour().getPageLimitSupport()
                .ifPresent(pageLimit -> pageLimit.pageOf((long)form.getPageNum(), (long)ref.size));
        }

        return q.pour().fetch().stream().map(entity -> {
            return new LogRecordVO(entity.getOperateType(), entity.getOperateContent(), entity.getOperateUserName(),
                entity.getOperateIp(), entity.getOperateTime(), entity.getOperateResultJson(),
                entity.getOperateParamJson(), entity.getOperateError(), entity.getOperateErrorLevel(),
                new Date(entity.getOperateTime().getTime() + entity.getCostTime()), entity.getOperateUrl(),
                entity.getMethod(), entity.getCostTime());
        }).

            collect(Collectors.toList());
    }

    @Override
    public int count(LoggingRecordExtractForm form) {
        if (form == null) {
            return 0;
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
        return q.pour().fetch().size();
    }

    /**
     * 获取所有日志类型(最近3个月)
     *
     * @return 日志类型列表
     */
    @Override
    @Cacheable(value = ALL_OPERATE_TYPES, key = "'allOperateTypes'")
    @ExpireOn(expireAll = true, value = 1, timeUnit = TimeUnit.HOURS)
    public List<String> getAllOperateTypes() {
        // 查询最近3个月的数据
        Date today = new Date();
        Date threeMonthsAgo = DateUtil.offsetMonth(today, -3).toJdkDate();

        var q = QueryEngine.getEntityQueryWrapper(new SystemOperateRecordEntity()).query().where()
            .greaterThanOrEqual(SystemOperateRecordEntity::getOperateTime, threeMonthsAgo).pour().fetch();

        if (CollUtil.isEmpty(q)) {
            return CollUtil.newArrayList();
        }

        // 提取不重复的操作类型并排序
        return q.stream().map(SystemOperateRecordEntity::getOperateType)
            .filter(type -> type != null && !type.trim().isEmpty()).distinct().sorted().collect(Collectors.toList());
    }
}
