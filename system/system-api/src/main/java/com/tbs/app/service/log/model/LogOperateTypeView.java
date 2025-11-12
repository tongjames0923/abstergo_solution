package com.tbs.app.service.log.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 操作类型报表模型
 */
@Data
public class LogOperateTypeView implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 操作类型，对应操作执行的函数名
     */
    private String operateType;
    /**
     * 操作次数
     */
    private BigDecimal operateCount;
    /**
     * 操作执行总时间
     */
    private BigDecimal operateTime;

    /**
     * 操作执行最长时间
     */
    private BigDecimal operateTimeMax;

    /**
     * 操作执行最短时间
     */
    private BigDecimal operateTimeMin;

    /**
     * 操作执行错误次数
     */
    private BigDecimal errorCount;

}
