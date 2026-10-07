package com.tbs.common.data;

import com.tbs.common.data.structs.TableData;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 表格数据简单操作库
 * <p>
 * 面向 {@link TableData} 的无状态纯函数集合：结构摘要、数值列统计、行采样。
 * 全部操作不修改入参（TableData 本身不可变），仅产出报告值对象，
 * 供本地或远程 {@link ITableDataHandler} 实现组合使用。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class TableDataOperators {

    /**
     * 数值统计输出的均值小数位
     */
    private static final int AVG_SCALE = 4;

    private TableDataOperators() {
    }

    /**
     * 数值列统计结果
     * <p>
     * 不可变值对象：count 为该列可解析为数值的单元格个数，
     * count 为 0 时 sum/min/max 为 null（即非数值列）。
     * </p>
     */
    @Getter
    @ToString
    public static final class NumericStats {

        /**
         * 数值单元格个数
         */
        private final int count;

        /**
         * 数值总和
         */
        private final BigDecimal sum;

        /**
         * 数值最小值
         */
        private final BigDecimal min;

        /**
         * 数值最大值
         */
        private final BigDecimal max;

        NumericStats(int count, BigDecimal sum, BigDecimal min, BigDecimal max) {
            this.count = count;
            this.sum = sum;
            this.min = min;
            this.max = max;
        }

        /**
         * 数值均值，非数值列返回 null
         *
         * @return 均值（保留 {@value TableDataOperators#AVG_SCALE} 位小数），count 为 0 时为 null
         */
        public BigDecimal getAvg() {
            return count == 0 ? null : sum.divide(BigDecimal.valueOf(count), AVG_SCALE, RoundingMode.HALF_UP);
        }
    }

    /**
     * 结构摘要
     * <p>
     * 输出表格标识、列数、行数与列名清单的单行摘要文本。
     * </p>
     *
     * @param table 表格数据，不能为null
     * @return 摘要文本
     */
    public static String summary(TableData table) {
        StringBuilder sb = new StringBuilder()
            .append("Table[").append(table.getTableId())
            .append('/').append(table.getTableName()).append("] ")
            .append("columns=").append(table.getColumns().size())
            .append(" rows=").append(table.getRows().size())
            .append(" names=");
        for (int i = 0; i < table.getColumns().size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(table.getColumns().get(i).getName());
        }
        return sb.toString();
    }

    /**
     * 数值列统计
     * <p>
     * 逐列按行解析单元格（单元格按列位置与列对齐），可解析为 {@link BigDecimal} 的
     * 计入 count/sum/min/max，均值经 {@link NumericStats#getAvg()} 派生；
     * 空白与非数值单元格跳过，整列无一可解析时视为非数值列（count=0）。
     * </p>
     *
     * @param table 表格数据，不能为null
     * @return 列名到统计结果的映射，保持列定义顺序
     */
    public static Map<String, NumericStats> numericStats(TableData table) {
        Map<String, NumericStats> result = new LinkedHashMap<>();
        for (TableData.Column column : table.getColumns()) {
            int count = 0;
            BigDecimal sum = null;
            BigDecimal min = null;
            BigDecimal max = null;
            for (TableData.Row row : table.getRows()) {
                List<String> cells = row.getCells();
                if (column.getIndex() == null || column.getIndex() >= cells.size()) {
                    continue;
                }
                BigDecimal value = parseNumber(cells.get(column.getIndex()));
                if (value == null) {
                    continue;
                }
                count++;
                sum = sum == null ? value : sum.add(value);
                min = min == null || value.compareTo(min) < 0 ? value : min;
                max = max == null || value.compareTo(max) > 0 ? value : max;
            }
            result.put(column.getName(), new NumericStats(count, sum, min, max));
        }
        return result;
    }

    /**
     * 行采样
     * <p>
     * 返回表格前 limit 行，行数不足时返回全部行。
     * </p>
     *
     * @param table 表格数据，不能为null
     * @param limit 采样行数上限，非正数返回空列表
     * @return 采样行列表
     */
    public static List<TableData.Row> sampleRows(TableData table, int limit) {
        List<TableData.Row> rows = table.getRows();
        return rows.subList(0, Math.max(0, Math.min(limit, rows.size())));
    }

    /**
     * 将单元格文本解析为数值
     *
     * @param cell 单元格文本，可为null
     * @return 解析结果，空白或非法数字返回 null
     */
    private static BigDecimal parseNumber(String cell) {
        if (cell == null || cell.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(cell.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
