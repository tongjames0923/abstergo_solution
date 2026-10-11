package com.tbs.cdc.sqlserver.converter;

import cn.hutool.core.util.StrUtil;
import com.tbs.cdc.i18n.CdcI18nConstant;
import com.tbs.cdc.model.CdcChangeType;
import com.tbs.cdc.model.CdcEvent;
import com.tbs.cdc.model.CdcTransaction;
import com.tbs.cdc.property.CdcProperty;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.JsonUtils;
import io.debezium.engine.ChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Debezium变更事件到统一CdcEvent的转换器（SQL Server）
 * <p>
 * 解析Debezium JSON信封（key为变更行主键、value为变更数据信封），提取变更类型、库表、
 * 主键、前后数据、时间戳、事务与LSN位点，映射为数据库无关的{@link CdcEvent}。
 * SQL Server的source节点字段为{@code db}/{@code schema}/{@code table}/{@code lsn}，
 * 与MySQL的{@code file}/{@code pos}/{@code gtid}不同，故位点与事务按LSN语义映射。
 * 按{@code payload-mode}对载荷进行裁剪以降低内存与序列化开销。删除事件的墓碑记录
 * （value为null）返回null，由调用方跳过。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "tbs.cdc", name = "enable", havingValue = "true", matchIfMissing = true)
public class DebeziumEventConverter {

    /**
     * 捕获源类型标识，由实现模块内置决定，不由配置声明（避免与实际运行实现不一致）
     */
    private static final String SOURCE_TYPE = "sqlserver";

    @Resource
    private CdcProperty cdcProperty;

    /**
     * 将Debezium字符串键值变更事件转换为统一变更事件
     *
     * @param changeEvent Debezium变更事件，key/value为JSON字符串
     * @return 统一变更事件；墓碑事件（value为空）返回null
     */
    public CdcEvent convert(ChangeEvent<String, String> changeEvent) {
        String valueJson = changeEvent.value();
        // 删除操作的墓碑(tombstone)事件value为空，无数据载荷，跳过
        if (StrUtil.isBlank(valueJson)) {
            return null;
        }
        Map<String, Object> envelope = JsonUtils.parseMap(valueJson);
        AssertUtil.isTrue(envelope == null).i18n(CdcI18nConstant.CONVERT_FAILED, abbreviate(valueJson))
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        Map<String, Object> payload = asMap(envelope.get("payload"));
        if (payload == null) {
            return null;
        }

        CdcEvent event = new CdcEvent();
        event.setChangeType(mapChangeType(str(payload.get("op"))));
        event.setSourceType(SOURCE_TYPE);

        Map<String, Object> source = asMap(payload.get("source"));
        if (source != null) {
            event.setDatabase(str(source.get("db")));
            event.setTable(str(source.get("table")));
            event.setSourceTimestamp(toLong(source.get("ts_ms")));
            event.setTransaction(buildTransaction(source, payload));
            event.setPosition(buildPosition(source));
        }
        event.setCaptureTimestamp(toLong(payload.get("ts_ms")));
        event.setPrimaryKey(extractPrimaryKey(changeEvent.key()));
        event.setBeforeData(asMap(payload.get("before")));
        event.setAfterData(asMap(payload.get("after")));

        applyPayloadMode(event);
        return event;
    }

    /**
     * 将Debezium操作码映射为变更类型
     *
     * @param op 操作码：c/u/d/r
     * @return 变更类型，未知回退为UPDATE
     */
    private CdcChangeType mapChangeType(String op) {
        if (op == null) {
            return CdcChangeType.UPDATE;
        }
        return switch (op) {
            case "c" -> CdcChangeType.CREATE;
            case "d" -> CdcChangeType.DELETE;
            case "r" -> CdcChangeType.READ;
            default -> CdcChangeType.UPDATE;
        };
    }

    /**
     * 构建事务信息
     * <p>
     * SQL Server无MySQL的gtid/binlog语义，以{@code commit_lsn}作为事务标识，
     * {@code lsn}/{@code change_version}记录行级位点。
     * </p>
     *
     * @param source  source节点
     * @param payload payload节点
     * @return 事务信息
     */
    private CdcTransaction buildTransaction(Map<String, Object> source, Map<String, Object> payload) {
        CdcTransaction transaction = new CdcTransaction();
        transaction.setBinlogFileName(str(source.get("commit_lsn")));
        transaction.setPosition(toLong(source.get("event_serial_no")));
        Map<String, Object> tx = asMap(payload.get("transaction"));
        if (tx != null) {
            transaction.setTransactionOrder(toInteger(tx.get("data_collection_order")));
            transaction.setTotalOrder(toInteger(tx.get("total_order")));
        }
        return transaction;
    }

    /**
     * 构建位点标识串（SQL Server以LSN为准，回退change_version）
     *
     * @param source source节点
     * @return 位点串，均为空返回null
     */
    private String buildPosition(Map<String, Object> source) {
        String lsn = str(source.get("lsn"));
        if (StrUtil.isNotBlank(lsn)) {
            return lsn;
        }
        return str(source.get("change_version"));
    }

    /**
     * 从事件key提取主键列取值
     *
     * @param keyJson key JSON字符串
     * @return 主键映射，无则空Map
     */
    private Map<String, Object> extractPrimaryKey(String keyJson) {
        if (StrUtil.isBlank(keyJson)) {
            return Collections.emptyMap();
        }
        Map<String, Object> keyEnvelope = JsonUtils.parseMap(keyJson);
        if (keyEnvelope == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> keyPayload = asMap(keyEnvelope.get("payload"));
        return keyPayload == null ? Collections.emptyMap() : keyPayload;
    }

    /**
     * 按配置的载荷模式裁剪前后数据
     * <p>
     * key-only清空前后数据；changed-only对UPDATE仅保留差异列；full保持原样。
     * </p>
     *
     * @param event 变更事件
     */
    private void applyPayloadMode(CdcEvent event) {
        String mode = cdcProperty.getPayloadMode();
        if (CdcProperty.PayloadMode.KEY_ONLY.getValue().equalsIgnoreCase(mode)) {
            event.setBeforeData(Collections.emptyMap());
            event.setAfterData(Collections.emptyMap());
            return;
        }
        if (CdcProperty.PayloadMode.CHANGED_ONLY.getValue().equalsIgnoreCase(mode)
            && event.getChangeType() == CdcChangeType.UPDATE) {
            Set<String> changed = event.changedColumns();
            event.setBeforeData(prune(event.getBeforeData(), changed));
            event.setAfterData(prune(event.getAfterData(), changed));
        }
    }

    /**
     * 仅保留指定列的数据视图
     *
     * @param data    原始数据
     * @param columns 保留列集合
     * @return 裁剪后的数据，入参为空返回空Map
     */
    private Map<String, Object> prune(Map<String, Object> data, Set<String> columns) {
        if (data == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> pruned = new LinkedHashMap<>();
        for (String column : columns) {
            if (data.containsKey(column)) {
                pruned.put(column, data.get(column));
            }
        }
        return pruned;
    }

    /**
     * 安全转换为Map
     *
     * @param obj 待转换对象
     * @return Map，非Map返回null
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object obj) {
        return obj instanceof Map ? (Map<String, Object>) obj : null;
    }

    /**
     * 安全转换为字符串
     *
     * @param obj 待转换对象
     * @return 字符串，null返回null
     */
    private String str(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }

    /**
     * 安全转换为Long
     *
     * @param obj 待转换对象
     * @return Long，无法解析返回null
     */
    private Long toLong(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(obj));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 安全转换为Integer
     *
     * @param obj 待转换对象
     * @return Integer，无法解析返回null
     */
    private Integer toInteger(Object obj) {
        Long value = toLong(obj);
        return value == null ? null : value.intValue();
    }

    /**
     * 截断过长的原始载荷用于异常上下文
     *
     * @param json 原始JSON
     * @return 截断后的字符串
     */
    private String abbreviate(String json) {
        return json.length() > 200 ? json.substring(0, 200) + "..." : json;
    }
}
