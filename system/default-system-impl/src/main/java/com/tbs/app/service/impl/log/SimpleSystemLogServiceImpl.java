package com.tbs.app.service.impl.log;

import cn.hutool.core.date.DateUtil;
import com.tbs.app.service.log.ISystemLogService;
import com.tbs.app.service.log.model.LogOperateTypeView;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.mapper.SystemOperateMapper;
import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
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
        Map<String, Set<String>> userMap = new HashMap<>(); // 记录每个操作类型的用户数
        Map<String, Set<String>> ipMap = new HashMap<>();   // 记录每个操作类型的IP数
        Map<String, List<Long>> timeListMap = new HashMap<>(); // 记录每个操作类型的时间列表用于计算分位数

        for (SystemOperateRecordEntity operateRecordEntity : data) {
            String operateType = operateRecordEntity.getOperateType();
            Long costTime = operateRecordEntity.getCostTime();
            String userName = operateRecordEntity.getOperateUserName();
            String ip = operateRecordEntity.getOperateIp();

            // 收集用户数和IP数
            userMap.computeIfAbsent(operateType, k -> new HashSet<>()).add(userName);
            ipMap.computeIfAbsent(operateType, k -> new HashSet<>()).add(ip);

            // 收集时间列表用于计算分位数
            timeListMap.computeIfAbsent(operateType, k -> new ArrayList<>()).add(costTime);

            LogOperateTypeView logOperateTypeView = dataMap.get(operateRecordEntity.getOperateType());
            if (logOperateTypeView == null) {
                logOperateTypeView = new LogOperateTypeView();
                logOperateTypeView.setOperateCount(BigDecimal.ONE);
                var costTimeDecimal = BigDecimal.valueOf(operateRecordEntity.getCostTime());
                logOperateTypeView.setOperateTime(new BigDecimal(costTimeDecimal.toString()));
                if (StrUtil.isNotEmpty(operateRecordEntity.getOperateError())) {
                    logOperateTypeView.setErrorCount(BigDecimal.ONE);
                } else {
                    logOperateTypeView.setErrorCount(BigDecimal.ZERO);
                }
                logOperateTypeView.setOperateTimeMax(new BigDecimal(costTimeDecimal.toString()));
                logOperateTypeView.setOperateTimeMin(new BigDecimal(costTimeDecimal.toString()));
                logOperateTypeView.setOperateType(operateRecordEntity.getOperateType());
                dataMap.put(operateRecordEntity.getOperateType(), logOperateTypeView);
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

        // 计算额外的统计数据
        for (Map.Entry<String, LogOperateTypeView> entry : dataMap.entrySet()) {
            String operateType = entry.getKey();
            LogOperateTypeView view = entry.getValue();

            // 设置用户数和IP数
            view.setUserCount(BigDecimal.valueOf(userMap.get(operateType).size()));
            view.setIpCount(BigDecimal.valueOf(ipMap.get(operateType).size()));

            // 计算成功率
            BigDecimal successCount = view.getOperateCount().subtract(view.getErrorCount());
            if (view.getOperateCount().compareTo(BigDecimal.ZERO) > 0) {
                view.setSuccessRate(successCount.divide(view.getOperateCount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)));
            } else {
                view.setSuccessRate(BigDecimal.ZERO);
            }

            // 计算分位数
            List<Long> timeList = timeListMap.get(operateType);
            if (timeList != null && !timeList.isEmpty()) {
                Collections.sort(timeList);

                // 计算中位数
                long median = timeList.get(timeList.size() / 2);
                view.setOperateTimeMedian(BigDecimal.valueOf(median));

                // 计算90%分位数
                int index90 = (int)(timeList.size() * 0.9);
                // 确保索引不越界
                if (index90 >= timeList.size()) {
                    index90 = timeList.size() - 1;
                }
                long time90Percentile = timeList.get(index90);
                view.setOperateTime90Percentile(BigDecimal.valueOf(time90Percentile));

                // 计算标准差
                if (view.getOperateCount().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal avgTime = view.getOperateTime().divide(view.getOperateCount(), 4, RoundingMode.HALF_UP);
                    BigDecimal sumSquareDifferences =
                        timeList.stream().map(time -> BigDecimal.valueOf(time).subtract(avgTime))
                            .map(diff -> diff.multiply(diff)).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal variance =
                        sumSquareDifferences.divide(BigDecimal.valueOf(timeList.size()), 4, RoundingMode.HALF_UP);
                    view.setOperateTimeStdDev(BigDecimal.valueOf(Math.sqrt(variance.doubleValue())));
                } else {
                    view.setOperateTimeStdDev(BigDecimal.ZERO);
                }
            } else {
                view.setOperateTimeMedian(BigDecimal.ZERO);
                view.setOperateTime90Percentile(BigDecimal.ZERO);
                view.setOperateTimeStdDev(BigDecimal.ZERO);
            }
        }

        return dataMap.values().stream().map((v) -> {
            v.setOperateType(suitTypeName(v.getOperateType()));
            return v;
        }).collect(Collectors.toList());

    }
}
