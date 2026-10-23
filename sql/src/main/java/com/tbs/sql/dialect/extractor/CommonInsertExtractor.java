package com.tbs.sql.dialect.extractor;

import com.tbs.sql.dialect.IInsertExtractor;
import com.tbs.sql.dialect.oracle.extractor.OracleInsertExtractor;

import java.util.List;

/**
 * 通用INSERT语句提取器，生成标准多行 VALUES 语法的批量插入语句。
 *
 * <p>适用于 MySQL、PostgreSQL 以及支持 table value constructor 的 SQL Server
 * （2008 及以上版本）。以竖线拼接列名清单与参数占位符，不依赖任何数据库特有函数。</p>
 *
 * <h3>生成示例</h3>
 * <pre>{@code
 * INSERT INTO t_user (id, user_name) VALUES (#{records[0].id}, #{records[0].userName}),
 * (#{records[1].id}, #{records[1].userName})
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>占位符中的 {@code records} 是批量插入方法的固定参数名，调用方必须保持一致</li>
 *   <li>调用方需按 {@link #maxRows()} 与 {@link #maxParameters()} 自行分批</li>
 * </ul>
 *
 * @author abstergo
 * @since 3.0.0
 * @see IInsertExtractor
 * @see com.tbs.sql.dialect.oracle.extractor.OracleInsertExtractor
 */
public class CommonInsertExtractor implements IInsertExtractor {

    @Override
    public String buildBatchInsert(String tableName, List<String> columns, List<String> properties, int rowCount) {
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        builder.append(tableName).append(" (");
        builder.append(String.join(", ", columns));
        builder.append(") VALUES ");
        for (int row = 0; row < rowCount; row++) {
            if (row > 0) {
                builder.append(", ");
            }
            builder.append('(');
            OracleInsertExtractor.buildProps(properties, builder, row);
        }
        return builder.toString();
    }
}
