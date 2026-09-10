package com.tbs.common.data.structs;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 表格数据
 * <p>
 * 不可变数据结构，通过 {@link Builder} 链式构建。构建完成后所有字段不可修改，
 * 列表字段经 {@link Collections#unmodifiableList(List)} 包装，天然线程安全。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * TableData table = TableData.builder()
 *     .tableId("order-001")
 *     .tableName("订单明细")
 *     .addColumn("商品名称", 0)
 *     .addColumn("数量", 1)
 *     .addRow(Arrays.asList("手机", "2"), 0)
 *     .build();
 * }</pre>
 *
 * @author abstergo
 */
@Getter
@ToString
@EqualsAndHashCode
public class TableData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 表格ID
     */
    private final String tableId;

    /**
     * 表格名称
     */
    private final String tableName;

    /**
     * 列定义列表，构建后不可变
     */
    private final List<Column> columns;

    /**
     * 行数据列表，构建后不可变
     */
    private final List<Row> rows;

    /**
     * 私有构造，仅由 {@link Builder#build()} 调用
     *
     * @param tableId   表格ID
     * @param tableName 表格名称
     * @param columns   列定义列表（已不可变）
     * @param rows      行数据列表（已不可变）
     */
    private TableData(String tableId, String tableName, List<Column> columns, List<Row> rows) {
        this.tableId = tableId;
        this.tableName = tableName;
        this.columns = columns;
        this.rows = rows;
    }

    /**
     * 创建构建器
     *
     * @return Builder 实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 列定义
     * <p>
     * 不可变值对象，字段均为 final。
     * </p>
     */
    @Getter
    @ToString
    @AllArgsConstructor
    public static final class Column implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 列名
         */
        private final String name;

        /**
         * 列号
         */
        private final Integer index;

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Column)) {
                return false;
            }
            Column column = (Column) o;
            return Objects.equals(getName(), column.getName()) && Objects.equals(getIndex(), column.getIndex());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName(), getIndex());
        }
    }

    /**
     * 行数据
     * <p>
     * 不可变值对象，字段均为 final。
     * </p>
     */
    @Getter
    @ToString
    @AllArgsConstructor
    @EqualsAndHashCode
    public static final class Row implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 单元格数据，按列存储，每列对应一个单元格数据
         */
        private final List<String> cells;

        /**
         * 行号
         */
        private final Integer index;
    }

    /**
     * 表格数据构建器
     * <p>
     * 链式 API，支持延迟初始化（列表在首次 add 时创建）、参数校验
     * （列/行序号非负且唯一）、不可变结果产出。
     * </p>
     * <p>
     * 性能优化：
     * <ul>
     *   <li>延迟初始化：columns/rows 的 ArrayList 仅在首次 add 时创建</li>
     *   <li>空列表优化：无元素时 build() 返回 {@link Collections#emptyList()} 共享单例</li>
     *   <li>不可变包装：build() 中列表经 {@link Collections#unmodifiableList(List)} 包装</li>
     *   <li>防御性拷贝：批量入参方法对原始列表做拷贝，隔离外部修改</li>
     * </ul>
     *
     * @author abstergo
     */
    public static class Builder {

        /**
         * 表格ID
         */
        private String tableId;

        /**
         * 表格名称
         */
        private String tableName;

        /**
         * 列列表，null 表示延迟初始化
         */
        private List<Column> columns;

        /**
         * 行列表，null 表示延迟初始化
         */
        private List<Row> rows;

        /**
         * 设置表格ID
         *
         * @param tableId 表格ID
         * @return 当前构建器
         */
        public Builder tableId(String tableId) {
            this.tableId = tableId;
            return this;
        }

        /**
         * 设置表格名称
         *
         * @param tableName 表格名称
         * @return 当前构建器
         */
        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        /**
         * 添加列
         *
         * @param name  列名，不能为空
         * @param index 列号，非负且唯一
         * @return 当前构建器
         */
        public Builder addColumn(String name, int index) {
            return addColumn(new Column(name, index));
        }

        /**
         * 添加列
         * <p>
         * 即时校验 column 非空，列名校验与序号校验在 {@link #build()} 中统一执行。
         * </p>
         *
         * @param column 列定义，不能为 null
         * @return 当前构建器
         */
        public Builder addColumn(Column column) {
            AssertUtil.isTrue(column == null)
                .i18n("table.column.name.required")
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
                .throwIt();
            if (columns == null) {
                columns = new ArrayList<>();
            }
            columns.add(column);
            return this;
        }

        /**
         * 批量添加列
         * <p>
         * 使用容量提示创建内部列表，逐个经 {@link #addColumn(Column)} 校验后追加。
         * </p>
         *
         * @param columns 列列表，null 或空则跳过
         * @return 当前构建器
         */
        public Builder addColumns(List<Column> columns) {
            if (columns == null || columns.isEmpty()) {
                return this;
            }
            if (this.columns == null) {
                this.columns = new ArrayList<>(columns.size());
            }
            for (Column column : columns) {
                addColumn(column);
            }
            return this;
        }

        /**
         * 整体替换列列表（防御性拷贝）
         * <p>
         * 对入参做 {@code new ArrayList<>(input)} 拷贝，隔离外部修改。
         * 元素校验在 {@link #build()} 中统一执行。
         * </p>
         *
         * @param columns 列列表，null 则清空
         * @return 当前构建器
         */
        public Builder columns(List<Column> columns) {
            this.columns = (columns == null) ? null : new ArrayList<>(columns);
            return this;
        }

        /**
         * 添加行
         *
         * @param cells 单元格数据，不能为 null
         * @param index 行号，非负且唯一
         * @return 当前构建器
         */
        public Builder addRow(List<String> cells, int index) {
            return addRow(new Row(cells, index));
        }

        /**
         * 添加行
         * <p>
         * 即时校验 row 非空及 cells 非空，序号校验在 {@link #build()} 中统一执行。
         * </p>
         *
         * @param row 行数据，不能为 null
         * @return 当前构建器
         */
        public Builder addRow(Row row) {
            AssertUtil.isTrue(row == null)
                .i18n("table.row.cells.required")
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
                .throwIt();
            AssertUtil.isTrue(row.getCells() == null)
                .i18n("table.row.cells.required")
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
                .throwIt();
            if (rows == null) {
                rows = new ArrayList<>();
            }
            rows.add(row);
            return this;
        }

        /**
         * 批量添加行
         * <p>
         * 使用容量提示创建内部列表，逐个经 {@link #addRow(Row)} 校验后追加。
         * </p>
         *
         * @param rows 行列表，null 或空则跳过
         * @return 当前构建器
         */
        public Builder addRows(List<Row> rows) {
            if (rows == null || rows.isEmpty()) {
                return this;
            }
            if (this.rows == null) {
                this.rows = new ArrayList<>(rows.size());
            }
            for (Row row : rows) {
                addRow(row);
            }
            return this;
        }

        /**
         * 整体替换行列表（防御性拷贝）
         * <p>
         * 对入参做 {@code new ArrayList<>(input)} 拷贝，隔离外部修改。
         * 元素校验在 {@link #build()} 中统一执行。
         * </p>
         *
         * @param rows 行列表，null 则清空
         * @return 当前构建器
         */
        public Builder rows(List<Row> rows) {
            this.rows = (rows == null) ? null : new ArrayList<>(rows);
            return this;
        }

        /**
         * 构建不可变 TableData
         * <p>
         * 统一校验列/行序号非负且唯一，列表经不可变包装后产出。
         * </p>
         *
         * @return 不可变 TableData 实例
         */
        public TableData build() {
            List<Column> finalColumns = toImmutableColumns();
            List<Row> finalRows = toImmutableRows();
            return new TableData(tableId, tableName, finalColumns, finalRows);
        }

        /**
         * 校验列并产出不可变列表
         * <p>
         * 空列表返回 {@link Collections#emptyList()} 共享单例；
         * 非空列表校验列名非空、序号非负且唯一后，经防御性拷贝与不可变包装返回。
         * </p>
         *
         * @return 不可变列列表
         */
        private List<Column> toImmutableColumns() {
            if (columns == null || columns.isEmpty()) {
                return Collections.emptyList();
            }
            Set<Integer> seen = new HashSet<>(columns.size());
            for (Column col : columns) {
                AssertUtil.isTrue(col == null || StrUtil.isEmpty(col.getName()))
                    .i18n("table.column.name.required")
                    .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
                    .throwIt();
                Integer idx = col.getIndex();
                AssertUtil.isTrue(idx == null || idx < 0)
                    .i18n("table.column.index.invalid", idx)
                    .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
                    .throwIt();
                AssertUtil.isTrue(!seen.add(idx))
                    .i18n("table.column.index.duplicate", idx)
                    .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
                    .throwIt();
            }
            return Collections.unmodifiableList(new ArrayList<>(columns));
        }

        /**
         * 校验行并产出不可变列表
         * <p>
         * 空列表返回 {@link Collections#emptyList()} 共享单例；
         * 非空列表校验单元格非空、序号非负且唯一后，经防御性拷贝与不可变包装返回。
         * </p>
         *
         * @return 不可变行列表
         */
        private List<Row> toImmutableRows() {
            if (rows == null || rows.isEmpty()) {
                return Collections.emptyList();
            }
            Set<Integer> seen = new HashSet<>(rows.size());
            for (Row row : rows) {
                AssertUtil.isTrue(row == null || row.getCells() == null)
                    .i18n("table.row.cells.required")
                    .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
                    .throwIt();
                Integer idx = row.getIndex();
                AssertUtil.isTrue(idx == null || idx < 0)
                    .i18n("table.row.index.invalid", idx)
                    .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
                    .throwIt();
                AssertUtil.isTrue(!seen.add(idx))
                    .i18n("table.row.index.duplicate", idx)
                    .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
                    .throwIt();
            }
            return Collections.unmodifiableList(new ArrayList<>(rows));
        }
    }
}
