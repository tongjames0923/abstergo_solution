package com.tbs.cdc.model;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CdcEvent#changedColumns()} 差异列计算自检
 * <p>
 * 覆盖CREATE/DELETE全列视图与UPDATE逐列比较（含新增列、删除列、值变化列与未变列），
 * 作为该非平凡纯逻辑的最小可运行检查。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
class CdcEventTest {

    /**
     * CREATE应返回afterData全部列
     */
    @Test
    void createShouldReturnAllAfterColumns() {
        CdcEvent event = new CdcEvent();
        event.setChangeType(CdcChangeType.CREATE);
        event.setAfterData(mapOf("id", 1, "name", "a"));
        assertEquals(Set.of("id", "name"), event.changedColumns());
    }

    /**
     * DELETE应返回beforeData全部列
     */
    @Test
    void deleteShouldReturnAllBeforeColumns() {
        CdcEvent event = new CdcEvent();
        event.setChangeType(CdcChangeType.DELETE);
        event.setBeforeData(mapOf("id", 1, "name", "a"));
        assertEquals(Set.of("id", "name"), event.changedColumns());
    }

    /**
     * UPDATE仅返回值变化、新增或移除的列，未变列不计入
     */
    @Test
    void updateShouldReturnOnlyDiffColumns() {
        CdcEvent event = new CdcEvent();
        event.setChangeType(CdcChangeType.UPDATE);
        event.setBeforeData(mapOf("id", 1, "name", "old", "status", "A"));
        event.setAfterData(mapOf("id", 1, "name", "new", "extra", "X"));
        Set<String> changed = event.changedColumns();
        assertTrue(changed.contains("name"), "值变化列应命中");
        assertTrue(changed.contains("status"), "移除列应命中");
        assertTrue(changed.contains("extra"), "新增列应命中");
        assertTrue(!changed.contains("id"), "未变列不应命中");
    }

    /**
     * 前后数据任一为空时返回空集合
     */
    @Test
    void updateWithNullDataShouldReturnEmpty() {
        CdcEvent event = new CdcEvent();
        event.setChangeType(CdcChangeType.UPDATE);
        event.setBeforeData(null);
        event.setAfterData(mapOf("id", 1));
        assertTrue(event.changedColumns().isEmpty());
    }

    /**
     * 构造有序Map便于断言
     *
     * @param keysAndValues 交替的键值数组
     * @return 有序Map
     */
    private Map<String, Object> mapOf(Object... keysAndValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put(String.valueOf(keysAndValues[i]), keysAndValues[i + 1]);
        }
        return map;
    }
}
