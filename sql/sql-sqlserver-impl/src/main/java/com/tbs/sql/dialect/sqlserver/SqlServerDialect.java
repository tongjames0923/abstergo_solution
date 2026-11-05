package com.tbs.sql.dialect.sqlserver;

import com.tbs.sql.dialect.*;
import com.tbs.sql.dialect.extractor.CommonDeleteExtractor;
import com.tbs.sql.dialect.extractor.CommonUpdateExtractor;
import com.tbs.sql.dialect.sqlserver.extractor.SqlServerInsertExtractor;

/**
 * SQL Server方言实现类。
 *
 * <p>该类实现了 {@link SqlDialect} 接口，提供了SQL Server数据库的方言支持。
 * 通过注入的5个提取器，构建SQL Server特有的SQL片段生成能力。</p>
 *
 * <h3>与MySQL方言的主要区别</h3>
 * <ul>
 *   <li>分页语法：SQL Server使用 {@code OFFSET ... FETCH NEXT ... ROWS ONLY}，MySQL使用 {@code LIMIT offset, count}</li>
 *   <li>SQL Server要求分页查询必须包含ORDER BY子句</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see SqlDialect
 * @see com.tbs.sql.dialect.sqlserver.extractor.SqlServerSelectExtractor
 * @see com.tbs.sql.dialect.sqlserver.extractor.SqlServerLimitExtractor
 */
public class SqlServerDialect implements SqlDialect {

    private final IGroupExtractor groupExtractor;
    private final IOrderExtractor orderExtractor;
    private final ILimitExtractor limitExtractor;
    private final ISelectExtractor selectExtractor;
    private final IWhereExtractor whereExtractor;

    public SqlServerDialect(IGroupExtractor groupExtractor, IOrderExtractor orderExtractor,
            ILimitExtractor limitExtractor, ISelectExtractor selectExtractor,
            IWhereExtractor whereExtractor) {
        this.groupExtractor = groupExtractor;
        this.orderExtractor = orderExtractor;
        this.limitExtractor = limitExtractor;
        this.selectExtractor = selectExtractor;
        this.whereExtractor = whereExtractor;
    }

    @Override public IGroupExtractor getGroupExtractor() { return groupExtractor; }
    @Override public IOrderExtractor getOrderExtractor() { return orderExtractor; }
    @Override public ILimitExtractor getLimitExtractor() { return limitExtractor; }
    @Override public ISelectExtractor getSelectExtractor() { return selectExtractor; }
    @Override public IWhereExtractor getWhereExtractor() { return whereExtractor; }
    @Override public IInsertExtractor getInsertExtractor() { return new SqlServerInsertExtractor(); }
    @Override public IUpdateExtractor getUpdateExtractor() { return new CommonUpdateExtractor(); }
    @Override public IDeleteExtractor getDeleteExtractor() { return new CommonDeleteExtractor(); }
    @Override public String name() { return "sqlserver"; }
}
