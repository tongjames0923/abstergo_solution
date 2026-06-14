package com.tbs.sql.builder;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.function.SSupplier;

/**
 * 导入式SQL构建上下文
 *
 * @author abstergo
 * @since 2.0.0
 */
public class ImportedSqlBuildContext extends AbstractSqlBuildContext {
    private SSupplier<Object> sqlEntitySupplier;

    public ImportedSqlBuildContext(SSupplier<Object> sqlEntitySupplier) {
        AssertUtil.notNull(sqlEntitySupplier, "注册SQL执行环境必须配置有效的查询实体");
        this.sqlEntitySupplier = sqlEntitySupplier;
    }

    @Override
    protected Object provideSqlEntity() { return sqlEntitySupplier.get(); }
}
