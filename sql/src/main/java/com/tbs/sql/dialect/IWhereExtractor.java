package com.tbs.sql.dialect;

import com.tbs.sql.converter.IValueConverter;
import com.tbs.sql.model.where.WhereCompose;

import java.util.List;
import java.util.Map;

/**
 * WHERE语句提取器
 */
public interface IWhereExtractor {
    String extract(WhereCompose whereCompose, Map<String, Object> params);
    List<IValueConverter> getValueConverters();
}
