package com.tbs.sql.enums;

import com.tbs.sql.dialect.IIdentifierQuoter;

/**
 * 内置标识符引用符，覆盖框架支持的四库方言。
 *
 * <ul>
 *   <li>{@link #ANSI}：双引号 {@code "close"}，用于 Oracle、PostgreSQL</li>
 *   <li>{@link #MYSQL}：反引号 {@code `close`}，用于 MySQL</li>
 *   <li>{@link #SQL_SERVER}：方括号 {@code [close]}，用于 SQL Server</li>
 * </ul>
 *
 * <p>方括号不受 SQL Server 的 {@code QUOTED_IDENTIFIER} 开关影响，
 * 反引号是 MySQL 唯一不依赖 {@code ANSI_QUOTES} 的引用符。</p>
 *
 * @author abstergo
 * @since 3.0.0
 * @see IIdentifierQuoter
 */
public enum IdentifierQuoting implements IIdentifierQuoter {

    /** SQL 标准双引号 */
    ANSI("\"", "\""),

    /** MySQL 反引号 */
    MYSQL("`", "`"),

    /** SQL Server 方括号 */
    SQL_SERVER("[", "]");

    private final String prefix;
    private final String suffix;

    IdentifierQuoting(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public String quote(String identifier) {
        return prefix + identifier + suffix;
    }
}
