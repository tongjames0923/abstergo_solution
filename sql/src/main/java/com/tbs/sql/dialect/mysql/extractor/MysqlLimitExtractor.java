package com.tbs.sql.dialect.mysql.extractor;

import com.tbs.sql.dialect.ILimitExtractor;
import com.tbs.sql.model.limit.LimitModel;

public class MysqlLimitExtractor implements ILimitExtractor {
    @Override
    public String extract(LimitModel model) {
        if (model == null) return "";
        Long start = model.getOffset();
        Long size = model.getCount();
        if (size == null) return "";
        if (start == null) return " LIMIT " + size;
        return " LIMIT " + start + ", " + size;
    }
}
