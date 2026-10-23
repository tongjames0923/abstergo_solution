package com.tbs.sql.dialect.sqlserver.extractor;

import com.tbs.sql.dialect.extractor.CommonInsertExtractor;

/**
 * SQL Server 的INSERT语句提取器。
 *
 * <p>SQL Server 2008 起支持标准多行 {@code VALUES}（table value constructor），
 * 语法与 MySQL、PostgreSQL 一致，因此直接复用 {@link CommonInsertExtractor} 的生成逻辑。
 * 与通用实现的唯一差异是容量上限，需按 SQL Server 的硬限制收紧。</p>
 *
 * <h3>上限依据</h3>
 * <ul>
 *   <li>单条 {@code INSERT ... VALUES} 最多 1000 行，超出报错误 10738</li>
 *   <li>单条语句最多 2100 个绑定参数，超出报 RPC 协议错误</li>
 * </ul>
 *
 * @author abstergo
 * @since 3.0.0
 * @see CommonInsertExtractor
 */
public class SqlServerInsertExtractor extends CommonInsertExtractor {

    /**
     * 获取单条语句允许的最大行数。
     *
     * @return 最大行数，SQL Server 限制为1000
     */
    @Override
    public int maxRows() {
        return 1000;
    }

    /**
     * 获取单条语句允许的最大绑定参数数。
     *
     * @return 最大参数数，SQL Server 限制为2100
     */
    @Override
    public int maxParameters() {
        return 2100;
    }
}
