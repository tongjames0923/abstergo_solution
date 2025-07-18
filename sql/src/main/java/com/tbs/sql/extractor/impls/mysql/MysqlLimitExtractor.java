package com.tbs.sql.extractor.impls.mysql;

import com.tbs.sql.extractor.ILimitSqlExtractor;
import com.tbs.sql.model.SqlLimitModel;

/**
 * MySqlLimitExtractor
 *
 * @author abstergo
 */
public class MysqlLimitExtractor implements ILimitSqlExtractor {

    @Override
    public String extraLimitSql(SqlLimitModel model) {
        if (model == null) {
            return "";
        }
        Long start = model.getOffset();
        Long size = model.getCount();
        if (size == null) {
            return "";
        }
        if (start == null) {
            return " LIMIT " + size;
        }
        return " LIMIT " + start + ", " + size;
    }
}
