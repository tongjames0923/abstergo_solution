package com.tbs.cdc.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 统一变更事件
 * <p>
 * CDC捕获源将任意数据库的一次数据变更转换为本结构，跨数据库消息结构保持一致。
 * 事件携带变更类型、库表标识、主键、变更前后的数据、时间戳、事务信息与日志位点，
 * 供进程内监听器与MQ桥接消费。消费端应以{@code eventId}作为幂等去重键
 * （投递语义为至少一次，重启断点续传可能产生重复事件）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class CdcEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件全局唯一标识（雪花ID），消费端幂等去重键
     */
    private String eventId;

    /**
     * 变更类型
     */
    private CdcChangeType changeType;

    /**
     * 数据库（schema）名称
     */
    private String database;

    /**
     * 表名
     */
    private String table;

    /**
     * 主键列与取值，无主键表为空Map
     */
    private Map<String, Object> primaryKey;

    /**
     * 变更前的整行数据，CREATE时为空Map
     */
    private Map<String, Object> beforeData;

    /**
     * 变更后的整行数据，DELETE时为空Map
     */
    private Map<String, Object> afterData;

    /**
     * 数据源侧变更时间戳（毫秒），取自数据库日志，可能为空
     */
    private Long sourceTimestamp;

    /**
     * CDC捕获处理时间戳（毫秒）
     */
    private Long captureTimestamp;

    /**
     * 事务信息与日志位点
     */
    private CdcTransaction transaction;

    /**
     * 消费位点标识（如binlog文件+偏移），用于断点续传诊断
     */
    private String position;

    /**
     * 捕获源类型标识（mysql/postgresql/mongodb等）
     */
    private String sourceType;

    /**
     * 计算本次变更的差异列集合
     * <p>
     * 对比变更前后的数据，返回取值发生变化的列名；CREATE返回afterData全部列，
     * DELETE返回beforeData全部列。仅UPDATE且前后数据均存在时逐列比较。
     * </p>
     *
     * @return 差异列名集合，无差异时返回空集合
     */
    public Set<String> changedColumns() {
        if (changeType == CdcChangeType.CREATE) {
            return afterData == null ? Collections.emptySet() : new LinkedHashSet<>(afterData.keySet());
        }
        if (changeType == CdcChangeType.DELETE) {
            return beforeData == null ? Collections.emptySet() : new LinkedHashSet<>(beforeData.keySet());
        }
        if (beforeData == null || afterData == null) {
            return Collections.emptySet();
        }
        Set<String> changed = new LinkedHashSet<>();
        Set<String> columns = new LinkedHashSet<>(beforeData.keySet());
        columns.addAll(afterData.keySet());
        for (String column : columns) {
            if (!Objects.equals(beforeData.get(column), afterData.get(column))) {
                changed.add(column);
            }
        }
        return changed;
    }
}
