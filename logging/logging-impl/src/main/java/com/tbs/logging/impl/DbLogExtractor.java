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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tbs.logging.impl.CacheNamesConstants.ALL_OPERATE_TYPES;

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

        // 列表轻投影：排除 param/result/error 三个 longtext 大字段，避免整行大字段回传；
        // 列表状态列改用已有的 operate_error_level(errorCode，异常时写入、非大字段) 判定，详情由 getById 按需单查。
        return q.pour()
            .excludeAny(SystemOperateRecordEntity::getOperateParamJson,
                SystemOperateRecordEntity::getOperateResultJson,
                SystemOperateRecordEntity::getOperateError)
            .fetch().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public LogRecordVO getById(Long id) {
        if (id == null) {
            return null;
        }
        SystemOperateRecordEntity entity = QueryEngine.getEntityQueryWrapper(new SystemOperateRecordEntity()).query()
            .where().equal(SystemOperateRecordEntity::getId, id).pour().fetchOne();
        return entity == null ? null : toVO(entity);
    }

    /**
     * 实体转 VO。列表轻投影时三个 longtext 字段为 null，详情全量查询时回齐。
     *
     * @param entity 日志实体
     * @return 视图对象
     */
    private LogRecordVO toVO(SystemOperateRecordEntity entity) {
        return new LogRecordVO(entity.getId(), entity.getOperateType(), entity.getOperateContent(),
            entity.getOperateUserName(), entity.getOperateIp(), entity.getOperateTime(),
            entity.getOperateResultJson(), entity.getOperateParamJson(), entity.getOperateError(),
            entity.getOperateErrorLevel(),
            new Date(entity.getOperateTime().getTime() + entity.getCostTime()), entity.getOperateUrl(),
            entity.getMethod(), entity.getCostTime());
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
        return q.pour().count();
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

        // 仅需 operateType 一列，投影排除 param/result/error 等 longtext 大字段，避免整行大字段回传
        var q = QueryEngine.getEntityQueryWrapper(new SystemOperateRecordEntity()).query().where()
            .greaterThanOrEqual(SystemOperateRecordEntity::getOperateTime, threeMonthsAgo).pour()
            .select(SystemOperateRecordEntity::getOperateType)
            .fetch();

        if (CollUtil.isEmpty(q)) {
            return CollUtil.newArrayList();
        }

        // 提取不重复的操作类型并排序
        return q.stream().map(SystemOperateRecordEntity::getOperateType)
            .filter(type -> type != null && !type.trim().isEmpty()).distinct().sorted().collect(Collectors.toList());
    }
}
