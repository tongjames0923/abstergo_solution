package com.tbs.sql.dialect.oracle.extractor;

import com.tbs.sql.dialect.IInsertExtractor;

import java.util.List;

/**
 * Oracle 的INSERT语句提取器，生成 {@code INSERT ALL} 多行插入语句。
 *
 * <p>Oracle 在 23ai 之前不支持标准多行 {@code VALUES (...), (...)}，会报 ORA-00928。
 * 兼容写法是 {@code INSERT ALL} 逐行声明 {@code INTO} 子句，并以 {@code SELECT 1 FROM DUAL} 收尾。 该写法在 11g、12c、19c、21c 以及 23ai
 * 上均可执行。</p>
 *
 * <h3>生成示例</h3>
 * <pre>{@code
 * INSERT ALL INTO t_user (id, user_name) VALUES (#{records[0].id}, #{records[0].userName})
 * INTO t_user (id, user_name) VALUES (#{records[1].id}, #{records[1].userName})
 * SELECT 1 FROM DUAL
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>每行必须重复完整的表名与列清单，Oracle 不会复用首个 INTO 子句的定义</li>
 *   <li>整条语句是一个原子 DML，任一行违反约束则全部回滚</li>
 *   <li>单行绑定参数上限按保守值 999 控制，与 IN 列表的经典限制保持一致</li>
 * </ul>
 *
 * @author abstergo
 * @see IInsertExtractor
 * @since 3.0.0
 */
public class OracleInsertExtractor implements IInsertExtractor {

    @Override
    public String buildBatchInsert(String tableName, List<String> columns, List<String> properties, int rowCount) {
        String columnList = String.join(", ", columns);
        StringBuilder builder = new StringBuilder("INSERT ALL");
        for (int row = 0; row < rowCount; row++) {
            builder.append(" INTO ").append(tableName).append(" (").append(columnList).append(") VALUES (");
            buildProps(properties, builder, row);
        }
        builder.append(" SELECT 1 FROM DUAL");
        return builder.toString();
    }

    public static void buildProps(List<String> properties, StringBuilder builder, int row) {
        for (int i = 0; i < properties.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append("#{records[").append(row).append("].").append(properties.get(i)).append('}');
        }
        builder.append(')');
    }

    /**
     * 获取单条语句允许的最大行数。
     *
     * @return 最大行数，Oracle 保守取500
     */
    @Override
    public int maxRows() {
        return 500;
    }

    /**
     * 获取单条语句允许的最大绑定参数数。
     *
     * @return 最大参数数，Oracle 保守取999
     */
    @Override
    public int maxParameters() {
        return 999;
    }
}
