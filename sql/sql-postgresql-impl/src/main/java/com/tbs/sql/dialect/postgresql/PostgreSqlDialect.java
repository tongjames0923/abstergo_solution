package com.tbs.sql.dialect.postgresql;

import com.tbs.sql.dialect.*;
import com.tbs.sql.dialect.extractor.CommonDeleteExtractor;
import com.tbs.sql.dialect.extractor.CommonInsertExtractor;
import com.tbs.sql.dialect.extractor.CommonUpdateExtractor;

/**
 * PostgreSQL方言实现类。
 *
 * <p>该类实现了 {@link SqlDialect} 接口，提供了PostgreSQL数据库的方言支持。
 * 通过注入的5个提取器，构建PostgreSQL特有的SQL片段生成能力。</p>
 *
 * <h3>与MySQL/SQL Server的主要区别</h3>
 * <ul>
 *   <li>分页语法：PostgreSQL使用 {@code LIMIT count OFFSET offset}，MySQL使用 {@code LIMIT offset, count}，SQL Server使用 {@code OFFSET ... FETCH NEXT ... ROWS ONLY}</li>
 *   <li>PostgreSQL不要求分页查询必须包含ORDER BY（与MySQL一致，与SQL Server不同）</li>
 *   <li>标识符使用双引号（"table_name"），而非反引号（`table_name`）</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see SqlDialect
 * @see com.tbs.sql.dialect.postgresql.extractor.PostgreSqlSelectExtractor
 * @see com.tbs.sql.dialect.postgresql.extractor.PostgreSqlLimitExtractor
 */
public class PostgreSqlDialect implements SqlDialect {

    private final IGroupExtractor groupExtractor;
    private final IOrderExtractor orderExtractor;
    private final ILimitExtractor limitExtractor;
    private final ISelectExtractor selectExtractor;
    private final IWhereExtractor whereExtractor;

    public PostgreSqlDialect(IGroupExtractor groupExtractor, IOrderExtractor orderExtractor,
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
    @Override public IInsertExtractor getInsertExtractor() { return new CommonInsertExtractor(); }
    @Override public IUpdateExtractor getUpdateExtractor() { return new CommonUpdateExtractor(); }
    @Override public IDeleteExtractor getDeleteExtractor() { return new CommonDeleteExtractor(); }
    @Override public String name() { return "postgresql"; }
}
