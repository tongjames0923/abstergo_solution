package com.tbs.sql.dialect.sqlserver;

import com.tbs.sql.assembler.impl.DefaultSqlAssembler;
import com.tbs.sql.dialect.sqlserver.extractor.SqlServerLimitExtractor;
import com.tbs.common.util.base.StrUtil;

/**
 * SQL Server专用SQL组装器。
 *
 * <p>该类继承自 {@link DefaultSqlAssembler}，覆写了SQL片段的组装逻辑，
 * 以处理SQL Server特有的分页语法要求：当使用 {@code OFFSET ... FETCH NEXT} 分页时，
 * 必须同时存在ORDER BY子句，否则SQL Server会抛出语法错误。</p>
 *
 * <h3>核心逻辑</h3>
 * <p>当LIMIT子句非空（即存在分页）但ORDER BY子句为空时，自动补充
 * {@code ORDER BY (SELECT NULL)} 作为默认排序，确保SQL Server语法正确。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>使用SQL Server数据库时的分页查询</li>
 *   <li>通过 {@link com.tbs.sql.dialect.sqlserver.extractor.SqlServerLimitExtractor} 生成分页SQL时</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see DefaultSqlAssembler
 * @see SqlServerLimitExtractor
 */
public class SqlServerAssembler extends DefaultSqlAssembler {

    @Override
    public String assembleSelectSql(String selectSql, String whereSql, String orderSql,
            String groupSql, String limitSql) {
        // SQL Server要求OFFSET/FETCH必须搭配ORDER BY，若无则自动补充默认排序
        if (StrUtil.isNotEmpty(limitSql) && StrUtil.isEmpty(orderSql)) {
            orderSql = SqlServerLimitExtractor.DEFAULT_ORDER;
        }
        return super.assembleSelectSql(selectSql, whereSql, orderSql, groupSql, limitSql);
    }
}
