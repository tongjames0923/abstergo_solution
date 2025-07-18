package com.tbs.sql.extractor.impls.mysql;

import com.tbs.sql.extractor.IGroupSqlExtractor;
import com.tbs.sql.model.SqlGroupModel;

import java.util.List;

/**
 * mysql方言的分组sql提取器
 *
 * @author abstergo
 */
public class MysqlGroupExtractor implements IGroupSqlExtractor {

    @Override
    public String extraGroupBySql(List<SqlGroupModel> sqlGroupModels) {
        StringBuilder sb = new StringBuilder();
        for (SqlGroupModel sqlGroupModel : sqlGroupModels) {
            sb.append(sqlGroupModel.getFieldName());
            sb.append(",");
        }
        return " group by " + sb.substring(0, sb.length() - 1);
    }
}
