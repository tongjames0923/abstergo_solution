package com.tbs.sql.dialect.mysql;

import com.tbs.sql.dialect.*;
import com.tbs.sql.dialect.extractor.CommonDeleteExtractor;
import com.tbs.sql.dialect.extractor.CommonInsertExtractor;
import com.tbs.sql.dialect.extractor.CommonUpdateExtractor;
import com.tbs.sql.enums.IdentifierQuoting;

public class MysqlDialect implements SqlDialect {
    private final IGroupExtractor groupExtractor;
    private final IOrderExtractor orderExtractor;
    private final ILimitExtractor limitExtractor;
    private final ISelectExtractor selectExtractor;
    private final IWhereExtractor whereExtractor;

    public MysqlDialect(IGroupExtractor groupExtractor, IOrderExtractor orderExtractor,
            ILimitExtractor limitExtractor, ISelectExtractor selectExtractor, IWhereExtractor whereExtractor) {
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
    @Override public IIdentifierQuoter getIdentifierQuoter() { return IdentifierQuoting.MYSQL; }
    @Override public String name() { return "mysql"; }
}
