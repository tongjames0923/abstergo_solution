package com.tbs.app.service.impl.log;

import cn.hutool.core.date.DateUtil;
import com.tbs.app.service.log.ISystemLogService;
import com.tbs.app.service.log.model.LogOperateTypeView;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.interfaces.log.ISystemOperateLogViewService;
import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class SimpleDbSystemLogServiceImpl implements ISystemLogService {

    @Resource
    private ISystemOperateLogViewService systemOperateLog;

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
                for (var j = i + 1; j < type.length(); j++) {
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
        Date lastDayStart = DateUtil.offsetDay(today, -lastDay).toJdkDate();
        var data = systemOperateLog.listSystemOperateRecords(lastDayStart, today);
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
                logOperateTypeView.setStabilityIndex(0);
                dataMap.put(operateRecordEntity.getOperateType(), logOperateTypeView);
            } else {
                logOperateTypeView.setOperateCount(logOperateTypeView.getOperateCount().add(BigDecimal.ONE));
                logOperateTypeView.setOperateTime(
                    logOperateTypeView.getOperateTime().add(BigDecimal.valueOf(operateRecordEntity.getCostTime())));
                if (!isIgnoreError(operateRecordEntity)) {
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

        //计算稳定性指数
        for (Map.Entry<String, LogOperateTypeView> entry : dataMap.entrySet()) {
            LogOperateTypeView view = entry.getValue();

            // 1. 成功率权重 (40%)
            // 成功率越高，得分越高
            BigDecimal successRateScore = view.getSuccessRate().multiply(BigDecimal.valueOf(0.4));

            // 2. 响应时间稳定性权重 (30%)
            // 使用变异系数(CV)来衡量，CV越小越稳定
            // CV = 标准差 / 平均值
            BigDecimal cvScore;
            if (view.getOperateCount().compareTo(BigDecimal.ZERO) > 0 &&
                view.getOperateTime().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal avgTime = view.getOperateTime().divide(view.getOperateCount(), 6, RoundingMode.HALF_UP);
                if (avgTime.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal cv = view.getOperateTimeStdDev().divide(avgTime, 6, RoundingMode.HALF_UP);
                    // CV值越大，得分越低，最大得分为30分
                    cvScore = BigDecimal.valueOf(30).subtract(cv.multiply(BigDecimal.valueOf(10)));
                    // 确保不会得到负分
                    cvScore = cvScore.max(BigDecimal.ZERO);
                } else {
                    cvScore = BigDecimal.valueOf(30);
                }
            } else {
                cvScore = BigDecimal.valueOf(30); // 如果没有有效数据，默认满分
            }
            cvScore = cvScore.multiply(BigDecimal.valueOf(0.3));

            // 3. 错误分散度权重 (20%)
            // 如果所有错误都来自少数用户/IP，则系统可能存在问题
            // 错误分散度 = 错误数 / (用户数 + IP数)
            BigDecimal errorDispersionScore;
            if (view.getErrorCount().compareTo(BigDecimal.ZERO) > 0 &&
                view.getUserCount().add(view.getIpCount()).compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal errorDispersion =
                    view.getErrorCount().divide(view.getUserCount().add(view.getIpCount()), 6, RoundingMode.HALF_UP);
                // 错误越集中，得分越低
                errorDispersionScore = BigDecimal.valueOf(20).subtract(errorDispersion.multiply(BigDecimal.valueOf(5)));
                errorDispersionScore = errorDispersionScore.max(BigDecimal.ZERO); // 最低0分
            } else {
                // 没有错误或者没有用户/IP信息，默认满分
                errorDispersionScore = BigDecimal.valueOf(20);
            }
            errorDispersionScore = errorDispersionScore.multiply(BigDecimal.valueOf(0.2));

            // 4. 请求量权重 (10%)
            // 请求量越多，稳定性评估越可信，给予一定加分
            // 以100次为基准，低于100次按比例扣分
            BigDecimal volumeScore;
            if (view.getOperateCount().compareTo(BigDecimal.valueOf(100)) >= 0) {
                volumeScore = BigDecimal.valueOf(10);
            } else {
                volumeScore = view.getOperateCount().multiply(BigDecimal.valueOf(0.1));
            }
            volumeScore = volumeScore.multiply(BigDecimal.valueOf(0.1));

            // 计算总分
            BigDecimal stabilityIndex = successRateScore.add(cvScore).add(errorDispersionScore).add(volumeScore);

            // 确保总分在0-100之间
            stabilityIndex = stabilityIndex.min(BigDecimal.valueOf(100)).max(BigDecimal.ZERO);

            // 设置稳定性指数（转换为整数）
            view.setStabilityIndex(stabilityIndex.intValue());
        }

        return dataMap.values().stream().map((v) -> {
            v.setOperateType(suitTypeName(v.getOperateType()));
            return v;
        }).collect(Collectors.toList());

    }

    private static boolean isIgnoreError(SystemOperateRecordEntity operateRecordEntity) {
        return StrUtil.isEmpty(operateRecordEntity.getOperateError()) ||
            (operateRecordEntity.getOperateErrorLevel() != null &&
                operateRecordEntity.getOperateErrorLevel() < ErrorLevelEnum.DATA_ACCESS_ERROR.getCode());
    }
}
