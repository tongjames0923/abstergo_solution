package com.tbs.common.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.tbs.common.data.TableDataOperators.NumericStats;
import com.tbs.common.data.structs.TableData;

/**
 * 表格数据简单操作库自检
 * <p>
 * 覆盖：结构摘要计数、数值列统计（含非数值单元格跳过、纯文本列、空表）、
 * 均值派生、行采样边界（超出总行数、非正数上限）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
class TableDataOperatorsTest {

    /**
     * 构建测试表格：商品名称（文本列）、数量（部分非数值）、单价（全数值）
     *
     * @return 测试表格数据
     */
    private TableData buildTable() {
        return TableData.builder()
            .tableId("order-001")
            .tableName("订单明细")
            .addColumn("商品名称", 0)
            .addColumn("数量", 1)
            .addColumn("单价", 2)
            .addRow(Arrays.asList("显示器", "2", "1500.50"), 0)
            .addRow(Arrays.asList("键盘", "3", "99.90"), 1)
            .addRow(Arrays.asList("鼠标", "abc", "49.00"), 2)
            .build();
    }

    /**
     * 验证结构摘要的列数/行数/列名输出
     */
    @Test
    void testSummary() {
        String summary = TableDataOperators.summary(buildTable());
        assertTrue(summary.contains("columns=3"), summary);
        assertTrue(summary.contains("rows=3"), summary);
        assertTrue(summary.endsWith("商品名称,数量,单价"), summary);
    }

    /**
     * 验证数值列统计：混合列跳过非数值、全数值列、纯文本列与均值派生
     */
    @Test
    void testNumericStats() {
        NumericStats qty = TableDataOperators.numericStats(buildTable()).get("数量");
        assertEquals(2, qty.getCount());
        assertEquals(0, qty.getSum().compareTo(new BigDecimal("5")));
        assertEquals(0, qty.getMin().compareTo(new BigDecimal("2")));
        assertEquals(0, qty.getMax().compareTo(new BigDecimal("3")));
        assertEquals(0, qty.getAvg().compareTo(new BigDecimal("2.5")));

        NumericStats price = TableDataOperators.numericStats(buildTable()).get("单价");
        assertEquals(3, price.getCount());
        assertEquals(0, price.getSum().compareTo(new BigDecimal("1649.40")));

        NumericStats name = TableDataOperators.numericStats(buildTable()).get("商品名称");
        assertEquals(0, name.getCount());
        assertNull(name.getSum());
        assertNull(name.getAvg());
    }

    /**
     * 验证空表统计：所有列为非数值列
     */
    @Test
    void testNumericStatsOnEmptyTable() {
        TableData empty = TableData.builder()
            .tableId("empty")
            .tableName("空表")
            .addColumn("列A", 0)
            .build();
        Map<String, NumericStats> stats = TableDataOperators.numericStats(empty);
        assertEquals(1, stats.size());
        assertEquals(0, stats.get("列A").getCount());
    }

    /**
     * 验证行采样边界：超出总行数返回全部，非正数上限返回空
     */
    @Test
    void testSampleRows() {
        TableData table = buildTable();
        assertEquals(2, TableDataOperators.sampleRows(table, 2).size());
        assertEquals(3, TableDataOperators.sampleRows(table, 10).size());
        assertEquals(0, TableDataOperators.sampleRows(table, 0).size());
        assertEquals(0, TableDataOperators.sampleRows(table, -1).size());
    }
}
