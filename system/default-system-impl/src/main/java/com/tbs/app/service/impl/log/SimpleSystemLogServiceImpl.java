package com.tbs.app.service.impl.log;

import cn.hutool.core.date.DateUtil;
import com.tbs.app.service.log.ISystemLogService;
import com.tbs.app.service.log.model.LogOperateTypeView;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.mapper.SystemOperateMapper;
import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimpleSystemLogServiceImpl implements ISystemLogService {

    @Resource
    SystemOperateMapper systemOperateMapper;

    private String suitTypeName(String type) {

        //com.tbs.app.controller.privates.PrivateUserController.info
        //截取类名和方法名
        StringBuilder sb = new StringBuilder();
        int pt = 0;
        for (var i = type.length() - 1; i >= 0; i--) {
            var c = type.charAt(i);
            if (c == '.') {
                pt++;
            }
            if (pt == 2) {
                for (var j = i+1; j < type.length(); j++) {
                    sb.append(type.charAt(j));
                }
                break;
            }
        }
        return sb.toString();
    }

    @Override
    public List<LogOperateTypeView> getLogOperateTypeList(int lastDay) {
        Date today = new Date();
        //将today设置成00:00:00
        today = DateUtil.beginOfDay(today).toJdkDate();
        Date lastDayStart = DateUtil.offsetDay(today, -lastDay).toJdkDate();
        var data = systemOperateMapper.getQueryWrapper(new SystemOperateRecordEntity()).where()
            .greaterThan(SystemOperateRecordEntity::getOperateTime, lastDayStart)
            .lessThanOrEqual(SystemOperateRecordEntity::getOperateTime, today).pour().fetch();
        Map<String, LogOperateTypeView> dataMap = new HashMap<>();
        for (SystemOperateRecordEntity operateRecordEntity : data) {
            LogOperateTypeView logOperateTypeView = dataMap.get(operateRecordEntity.getOperateType());
            if (logOperateTypeView == null) {
                logOperateTypeView = new LogOperateTypeView();
                var view = new LogOperateTypeView();
                view.setOperateCount(BigDecimal.ONE);
                var costTime = BigDecimal.valueOf(operateRecordEntity.getCostTime());
                view.setOperateTime(new BigDecimal(costTime.toString()));
                if (StrUtil.isNotEmpty(operateRecordEntity.getOperateError())) {
                    view.setErrorCount(BigDecimal.ONE);
                } else {
                    view.setErrorCount(BigDecimal.ZERO);
                }
                view.setOperateTimeMax(new BigDecimal(costTime.toString()));
                view.setOperateTimeMin(new BigDecimal(costTime.toString()));
                view.setOperateType(operateRecordEntity.getOperateType());
                dataMap.put(operateRecordEntity.getOperateType(), view);
            } else {
                logOperateTypeView.setOperateCount(logOperateTypeView.getOperateCount().add(BigDecimal.ONE));
                logOperateTypeView.setOperateTime(
                    logOperateTypeView.getOperateTime().add(BigDecimal.valueOf(operateRecordEntity.getCostTime())));
                if (StrUtil.isNotEmpty(operateRecordEntity.getOperateError())) {
                    logOperateTypeView.setErrorCount(logOperateTypeView.getErrorCount().add(BigDecimal.ONE));
                }
                logOperateTypeView.setOperateTimeMax(
                    BigDecimal.valueOf(operateRecordEntity.getCostTime()).max(logOperateTypeView.getOperateTimeMax()));
                logOperateTypeView.setOperateTimeMin(
                    BigDecimal.valueOf(operateRecordEntity.getCostTime()).min(logOperateTypeView.getOperateTimeMin()));
                dataMap.put(operateRecordEntity.getOperateType(), logOperateTypeView);
            }
        }
        return dataMap.values().stream().map((v) -> {
            v.setOperateType(suitTypeName(v.getOperateType()));
            return v;
        }).collect(Collectors.toList());

    }
}
