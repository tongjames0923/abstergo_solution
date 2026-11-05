package com.tbs.sql.dialect.sqlserver;

import com.tbs.sql.dialect.extractor.CommonAssembler;

/**
 * SQL Server专用SQL组装器。
 *
 * <p>该类继承自 {@link CommonAssembler}，覆写了默认排序SQL，
 * 以处理SQL Server特有的分页语法要求：当使用 {@code OFFSET ... FETCH NEXT} 分页时，
 * 必须同时存在ORDER BY子句，否则SQL Server会抛出语法错误。</p>
 *
 * <h3>核心逻辑</h3>
 * <p>当LIMIT子句非空（即存在分页）但ORDER BY子句为空时，自动补充
 * {@code ORDER BY (SELECT NULL)} 作为默认排序，确保SQL Server语法正确。</p>
 *
 * @author abstergo
 * @since 2.0.0
 * @see com.tbs.sql.dialect.extractor.CommonAssembler
 * @see com.tbs.sql.dialect.sqlserver.extractor.SqlServerLimitExtractor
 */
public class SqlServerAssembler extends CommonAssembler {

    @Override
    protected String getDefaultOrderSql() {
        return com.tbs.sql.dialect.sqlserver.extractor.SqlServerLimitExtractor.DEFAULT_ORDER;
    }
}
