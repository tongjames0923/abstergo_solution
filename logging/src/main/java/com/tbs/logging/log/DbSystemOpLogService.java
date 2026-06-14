package com.tbs.logging.log;

import cn.hutool.core.date.DateUtil;
import com.tbs.common.data.IDataHolderManager;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.CollWrapper;
import com.tbs.lock.util.LockUtil;
import com.tbs.shared.interfaces.log.ISystemOperateLogSender;
import com.tbs.shared.interfaces.log.ISystemOperateLogViewService;
import com.tbs.shared.mapper.SystemOperateMapper;
import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;
import com.tbs.sql.utils.QueryUtil;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class DbSystemOpLogService implements ISystemOperateLogViewService, ISystemOperateLogSender, DisposableBean {
    @Resource
    private SystemOperateMapper systemOperateMapper;

    @Resource
    private IDataHolderManager dataHolderManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(DbSystemOpLogService.class);

    @Setter
    @Getter
    private int batchSize = 100;

    @Getter
    @Setter
    private String dataHolderName = "DB_SYS_OPER_RECORD_BUFFER";

    @Override
    @Async
    public void log(SystemOperateRecordEntity operateRecord) {
        opDataSource((ls) -> {
            ls.add(operateRecord);
            if (ls.size() >= getBatchSize()) {
                flush(ls);
            }
        });
    }

    @Override
    public void flush() {
        opDataSource(ls -> {
            flush(ls);
        });
    }

    private void flush(List<SystemOperateRecordEntity> list) {
        if (list.isEmpty()) {
            return;
        }
        LOGGER.info("flush data in to db.size={},batch size={}", list.size(), getBatchSize());
        CollUtil.of(list).partition(getBatchSize()).map(CollWrapper::toList).forEach(ls -> {
            int effect = systemOperateMapper.insertList(ls);
            LOGGER.info("insert list size={},effect size={}", ls.size(), effect);
        });

        list.clear();
    }

    private void opDataSource(Consumer<List<SystemOperateRecordEntity>> consumer) {
        LockUtil.getInstance().lockThen(this, () -> {
            dataHolderManager.getDataHolder(getDataHolderName()).ifPresent(dataHolder -> {
                List<SystemOperateRecordEntity> ls =
                    (List<SystemOperateRecordEntity>)dataHolder.get(getDataHolderName()).orElse(new ArrayList<>());
                consumer.accept(ls);
                dataHolder.set(getDataHolderName(), ls, true);
            });
        });
    }

    private static final SystemOperateRecordEntity SYSTEM_OPERATE_RECORD_ENTITY = new SystemOperateRecordEntity();

    @Override
    public List<SystemOperateRecordEntity> listSystemOperateRecords(Date beginTime, Date endTime) {
        if (beginTime == null) {
            beginTime = DateUtil.offsetDay(new Date(), -28);
            beginTime = DateUtil.beginOfDay(beginTime);
        }
        if (endTime == null) {
            endTime = DateUtil.beginOfDay(new Date());
        }
        AssertUtil.isTrue(endTime.before(beginTime)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .message("开始时间不能大于结束时间").throwIt();
        var query = QueryUtil.getEntityQueryWrapper(SYSTEM_OPERATE_RECORD_ENTITY).query().where()
            .greaterThanOrEqual(SystemOperateRecordEntity::getOperateTime, beginTime)
            .lessThanOrEqual(SystemOperateRecordEntity::getOperateTime, endTime).pour();
        query.getOrderSupport().ifPresent(order -> {
            order.orderByDesc(SystemOperateRecordEntity::getOperateTime);
        });
        return query.fetch();
    }

    @Override
    public void destroy() throws Exception {
        LOGGER.warn("destroy DbSystemOPLog... ");
        opDataSource((ls) -> {
            flush(ls);
            ls.clear();
        });
    }
}
