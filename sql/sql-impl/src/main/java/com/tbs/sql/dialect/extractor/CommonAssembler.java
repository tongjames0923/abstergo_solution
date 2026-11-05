package com.tbs.sql.dialect.extractor;

import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.assembler.impl.DefaultSqlAssembler;

/**
 * 通用SQL组装器，自动处理分页查询需要ORDER BY的数据库（SQL Server、Oracle）。
 *
 * <p>当LIMIT子句非空（即存在分页）但ORDER BY子句为空时，自动补充默认排序。
 * 默认排序SQL由子类通过 {@link #getDefaultOrderSql()} 方法提供，
 * 以适配不同数据库的语法要求。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>SQL Server：补充 {@code ORDER BY (SELECT NULL)}</li>
 *   <li>Oracle：补充 {@code ORDER BY (SELECT NULL FROM DUAL)}</li>
 *   <li>MySQL/PostgreSQL：不需要此类，使用 {@link DefaultSqlAssembler} 即可</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see DefaultSqlAssembler
 */
public abstract class CommonAssembler extends DefaultSqlAssembler {

    /**
     * 获取默认的ORDER BY子句，当查询有分页但无排序时自动补充。
     *
     * @return 默认ORDER BY子句SQL字符串
     */
    protected abstract String getDefaultOrderSql();

    @Override
    public String assembleSelectSql(String selectSql, String whereSql, String orderSql,
            String groupSql, String limitSql) {
        if (StrUtil.isNotEmpty(limitSql) && StrUtil.isEmpty(orderSql)) {
            orderSql = getDefaultOrderSql();
        }
        return super.assembleSelectSql(selectSql, whereSql, orderSql, groupSql, limitSql);
    }
}
