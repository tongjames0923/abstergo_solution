package com.tbs.sql.dialect.extractor;

import com.tbs.sql.dialect.IUpdateExtractor;

import java.util.List;

/**
 * 通用UPDATE语句提取器，生成标准按主键更新语句。
 *
 * <p>单表 UPDATE 在 MySQL / Oracle / PostgreSQL / SQL Server 语法一致，故各方言默认复用本实现，
 * 无差异时不覆写。SET 子句按列名与属性名一一对应生成 {@code 列 = #{entity.属性}}，
 * 并以主键列作为 WHERE 条件。</p>
 *
 * <h3>生成示例</h3>
 * <pre>{@code
 * UPDATE t_user SET user_name = #{entity.userName}, status = #{entity.status} WHERE id = #{entity.id}
 * }</pre>
 *
 * @author abstergo
 * @since 3.0.0
 * @see IUpdateExtractor
 */
public class CommonUpdateExtractor implements IUpdateExtractor {

    @Override
    public String buildUpdate(String tableName, List<String> columns, List<String> properties, String idColumn, String idProperty) {
        StringBuilder builder = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(columns.get(i)).append(" = #{entity.").append(properties.get(i)).append('}');
        }
        builder.append(" WHERE ").append(idColumn).append(" = #{entity.").append(idProperty).append('}');
        return builder.toString();
    }
}
