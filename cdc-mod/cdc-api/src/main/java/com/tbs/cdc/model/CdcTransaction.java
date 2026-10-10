package com.tbs.cdc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * CDC变更事件的事务信息
 * <p>
 * 承载一次数据变更所属事务的上下文与binlog位点，用于消费端排序、去重与断点续传诊断。
 * 跨数据库语义一致：非事务型数据源可将对应字段置空。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CdcTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 全局事务标识（GTID），无GTID的数据源可为空
     */
    private String gtid;

    /**
     * 事务内事件序号（从0开始）
     */
    private Integer transactionOrder;

    /**
     * 事务内事件总数，未知时为-1
     */
    private Integer totalOrder;

    /**
     * binlog文件名（MySQL），其他数据库为等价日志标识
     */
    private String binlogFileName;

    /**
     * binlog文件内偏移位点（MySQL）
     */
    private Long position;
}
