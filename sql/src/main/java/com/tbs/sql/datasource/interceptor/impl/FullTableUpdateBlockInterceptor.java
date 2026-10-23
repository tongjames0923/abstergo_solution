package com.tbs.sql.datasource.interceptor.impl;

import com.tbs.sql.datasource.decoration.SqlTextUtils;
import com.tbs.sql.datasource.interceptor.JdbcExecutionInterceptor;
import com.tbs.sql.datasource.model.InterceptVerdict;
import com.tbs.sql.datasource.model.JdbcExecuteContext;
import com.tbs.sql.datasource.model.SqlCommandKind;

/**
 * 内置全表 UPDATE / DELETE 防护拦截器：拒绝无 WHERE 条件的更新与删除语句。
 *
 * <p>由 {@code tbs.sql.enable-full-table-update-block=true} 开关启用，
 * 是 {@link JdbcExecutionInterceptor} 的参考实现，展示典型用法：
 * 按命令类型分流 + 轻量文本特征判断 + 返回携带 i18n key 的拒绝裁决。</p>
 *
 * <h3>判定规则</h3>
 * <ul>
 *   <li>命令类型为 UPDATE 或 DELETE</li>
 *   <li>SQL 归一化后不含 WHERE 关键字</li>
 * </ul>
 *
 * <h3>已知边界</h3>
 * <p>仅做词边界级别的 WHERE 特征判断，不解析子查询与函数体内的 where，
 * 存在理论上的误判可能；生产防护请结合排除规则与更严格的实现。</p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class FullTableUpdateBlockInterceptor implements JdbcExecutionInterceptor {

    /**
     * 全表操作拒绝文案资源 key
     */
    private static final String DENY_KEY = "datasource.intercept.full.table.update.block";

    @Override
    public InterceptVerdict interceptExecution(JdbcExecuteContext context) {
        SqlCommandKind kind = context.getCommandKind();
        if (kind != SqlCommandKind.UPDATE && kind != SqlCommandKind.DELETE) {
            return InterceptVerdict.allow();
        }
        if (SqlTextUtils.containsWhere(context.getSql())) {
            return InterceptVerdict.allow();
        }
        return InterceptVerdict.deny(DENY_KEY, kind.name());
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
