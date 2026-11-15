package com.tbs.sql.dialect;

import com.tbs.sql.enums.IdentifierQuoting;

/**
 * 数据库标识符引用 SPI：由方言提供字段符号。
 *
 * <p>MySQL 为 {@code `close`}，SQL Server 为 {@code [close]}，
 * Oracle / PostgreSQL 为 {@code "close"}。实现类只需负责「包裹」这一步，
 * 何时需要引用、旧引用符剥离等判断统一由
 * {@link com.tbs.sql.util.SqlIdentifierUtils} 处理。</p>
 *
 * <p>方言通过 {@link SqlDialect#getIdentifierQuoter()} 暴露实现，
 * 新增数据库时实现本接口并覆写该 getter 即可。</p>
 *
 * @author abstergo
 * @since 3.0.0
 * @see SqlDialect#getIdentifierQuoter()
 * @see IdentifierQuoting
 */
@FunctionalInterface
public interface IIdentifierQuoter {

    /**
     * 用当前方言的符号包裹裸标识符。
     *
     * @param identifier 裸标识符（不含引用符）
     * @return 引用后的标识符
     */
    String quote(String identifier);
}
