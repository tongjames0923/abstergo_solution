package com.tbs.sql.dialect.oracle;

import com.tbs.sql.dialect.*;
import com.tbs.sql.dialect.extractor.CommonDeleteExtractor;
import com.tbs.sql.dialect.extractor.CommonUpdateExtractor;
import com.tbs.sql.dialect.oracle.extractor.OracleInsertExtractor;

/**
 * Oracle方言实现类。
 *
 * <p>该类实现了 {@link SqlDialect} 接口，提供了Oracle数据库的方言支持。
 * 通过注入的5个提取器，构建Oracle特有的SQL片段生成能力。</p>
 *
 * <h3>与MySQL/SQL Server的主要区别</h3>
 * <ul>
 *   <li>分页语法：Oracle 12c+使用 {@code OFFSET ... ROWS FETCH NEXT ... ROWS ONLY}，与SQL Server语法相同</li>
 *   <li>Oracle要求分页查询必须包含ORDER BY子句（与SQL Server一致）</li>
 *   <li>标识符默认为大写，双引号包裹时区分大小写</li>
 *   <li>需要Oracle 12c或更高版本以支持现代分页语法</li>
 * </ul>
 *
 * @author abstergo
 * @since 2.0.0
 * @see SqlDialect
 * @see OracleInsertExtractor
 * @see com.tbs.sql.dialect.oracle.extractor.OracleLimitExtractor
 */
public class OracleDialect implements SqlDialect {

    private final IGroupExtractor groupExtractor;
    private final IOrderExtractor orderExtractor;
    private final ILimitExtractor limitExtractor;
    private final ISelectExtractor selectExtractor;
    private final IWhereExtractor whereExtractor;

    public OracleDialect(IGroupExtractor groupExtractor, IOrderExtractor orderExtractor,
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

    @Override
    public IInsertExtractor getInsertExtractor() {
        return new OracleInsertExtractor();
    }
    @Override public IUpdateExtractor getUpdateExtractor() { return new CommonUpdateExtractor(); }
    @Override public IDeleteExtractor getDeleteExtractor() { return new CommonDeleteExtractor(); }
    @Override public String name() { return "oracle"; }
}
