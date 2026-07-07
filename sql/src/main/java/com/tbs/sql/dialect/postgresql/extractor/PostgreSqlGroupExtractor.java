package com.tbs.sql.dialect.postgresql.extractor;

import com.tbs.common.util.base.CollUtil;
import com.tbs.sql.dialect.IGroupExtractor;
import com.tbs.sql.model.group.GroupModel;

import java.util.List;

/**
 * PostgreSQL GROUP BY语句提取器。
 *
 * <p>该类实现了 {@link IGroupExtractor} 接口，提供了PostgreSQL特有的GROUP BY语句提取功能。
 * PostgreSQL的GROUP BY语法与MySQL/SQL Server一致，支持多字段分组。</p>
 *
 * @author abstergo
 * @since 2.0.0
 * @see IGroupExtractor
 * @see GroupModel
 */
public class PostgreSqlGroupExtractor implements IGroupExtractor {

    @Override
    public String extract(List<GroupModel> groupModels) {
        if (CollUtil.isEmpty(groupModels)) return "";
        StringBuilder sb = new StringBuilder();
        for (GroupModel model : groupModels) {
            sb.append(model.getFieldName()).append(",");
        }
        if (sb.length() == 0) return "";
        return " GROUP BY " + sb.substring(0, sb.length() - 1);
    }
}
