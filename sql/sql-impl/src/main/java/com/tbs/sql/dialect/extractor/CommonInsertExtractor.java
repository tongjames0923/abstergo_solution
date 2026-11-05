package com.tbs.sql.dialect.extractor;

import com.tbs.sql.dialect.IInsertExtractor;

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
            buildProps(properties, builder, row);
        }
        return builder.toString();
    }

    /**
     * 拼接一行内各属性的预编译占位符 {@code #{records[行下标].属性名}}。
     *
     * <p>标准多行 {@code VALUES} 与 Oracle {@code INSERT ALL} 共用此占位符生成规则，
     * 故放在通用实现上，各方言提取器按需调用。</p>
     *
     * @param properties 属性名清单
     * @param builder    目标拼接缓冲区
     * @param row        行下标
     */
    public static void buildProps(List<String> properties, StringBuilder builder, int row) {
        for (int i = 0; i < properties.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append("#{records[").append(row).append("].").append(properties.get(i)).append('}');
        }
        builder.append(')');
    }
}
