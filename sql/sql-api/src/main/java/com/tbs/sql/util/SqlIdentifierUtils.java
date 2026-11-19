package com.tbs.sql.util;

import com.tbs.common.util.framework.SpringUtil;
import com.tbs.sql.dialect.IIdentifierQuoter;
import com.tbs.sql.dialect.SqlDialect;
import com.tbs.sql.enums.IdentifierQuoting;

import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

/**
 * SQL 标识符渲染工具，把 {@link IIdentifierQuoter} SPI 接入各 SQL 片段生成器。
 *
 * <p>实体注解里 {@code @Column(name = "\"CLOSE\"")} 这类写死的 ANSI 引用会先被剥离，
 * 再由运行期方言重新包裹；保留字（如别名 {@code close}）也会补上引用符。
 * 普通列名、函数与表达式保持原样，避免改变已有 SQL 语义。</p>
 *
 * @author abstergo
 * @since 3.0.0
 * @see IIdentifierQuoter
 * @see SqlDialect#getIdentifierQuoter()
 */
public final class SqlIdentifierUtils {

    /** 常见 SQL 保留字（大写），命中时列名与别名必须加引用符 */
    private static final Set<String> RESERVED_WORDS = Set.of(
        "ADD", "ALL", "ALTER", "AND", "ANY", "AS", "ASC", "AUTHORIZATION",
        "BACKUP", "BEGIN", "BETWEEN", "BREAK", "BROWSE", "BULK", "BY",
        "CASCADE", "CASE", "CHECK", "CHECKPOINT", "CLOSE", "CLUSTERED", "COLLATE", "COLUMN",
        "COMMIT", "COMPUTE", "CONSTRAINT", "CONTAINS", "CONTINUE", "CONVERT", "CREATE", "CROSS",
        "CURRENT", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR",
        "DATABASE", "DEALLOCATE", "DECLARE", "DEFAULT", "DELETE", "DENY", "DESC", "DISK",
        "DISTINCT", "DISTRIBUTED", "DOUBLE", "DROP", "DUMP",
        "ELSE", "END", "ERRLVL", "ESCAPE", "EXCEPT", "EXEC", "EXECUTE", "EXISTS", "EXIT",
        "FETCH", "FILE", "FILLFACTOR", "FOR", "FOREIGN", "FREETEXT", "FROM", "FULL", "FUNCTION",
        "GOTO", "GRANT", "GROUP",
        "HAVING", "HOLDLOCK",
        "IDENTITY", "IF", "IN", "INDEX", "INNER", "INSERT", "INTERSECT", "INTO", "IS",
        "JOIN", "KEY", "KILL",
        "LEFT", "LIKE", "LINENO", "LOAD",
        "MERGE", "MINUS", "NATIONAL", "NOCHECK", "NONCLUSTERED", "NOT", "NULL",
        "OF", "OFF", "OFFSETS", "ON", "OPEN", "OPTION", "OR", "ORDER", "OUTER", "OVER",
        "PERCENT", "PIVOT", "PLAN", "PRECISION", "PRIMARY", "PRINT", "PROC", "PROCEDURE", "PUBLIC",
        "RAISERROR", "READ", "READTEXT", "RECONFIGURE", "REFERENCES", "REPLICATION", "RESTORE",
        "RESTRICT", "RETURN", "REVERT", "REVOKE", "RIGHT", "ROLLBACK", "ROWCOUNT", "ROWGUIDCOL", "RULE",
        "SAVE", "SCHEMA", "SELECT", "SESSION_USER", "SET", "SETUSER", "SHUTDOWN", "SOME", "STATISTICS",
        "SYSTEM_USER",
        "TABLE", "TABLESAMPLE", "TEXTSIZE", "THEN", "TO", "TOP", "TRAN", "TRANSACTION", "TRIGGER",
        "TRUNCATE", "TSEQUAL",
        "UNION", "UNIQUE", "UNPIVOT", "UPDATE", "UPDATETEXT", "USE", "USER",
        "VALUES", "VARYING", "VIEW",
        "WAITFOR", "WHEN", "WHERE", "WHILE", "WITH", "WRITETEXT",
        "CHARACTER", "POSITION", "SIZE", "LEVEL", "ROWS", "RANGE", "LIMIT", "OFFSET", "WINDOW",
        "PARTITION", "LATERAL", "RETURNING", "ANALYZE", "ARRAY", "ASYMMETRIC", "SYMMETRIC",
        "BOTH", "LEADING", "TRAILING", "LOCALTIME", "LOCALTIMESTAMP", "NEW", "OLD", "ONLY",
        "PLACING", "SIMILAR", "VERBOSE", "QUALIFY", "START", "CONNECT", "PRIOR", "ROWNUM",
        "TRUE", "FALSE", "DATE", "TIME", "TIMESTAMP", "INTERVAL", "VALUE");

    private static final Consumer<Throwable> IGNORE = e -> { };

    private SqlIdentifierUtils() {
    }

    /**
     * 渲染列名。
     *
     * @param column 列名，可含表别名限定（如 {@code t.CLOSE}）
     * @return 可直接拼入 SQL 的列名文本
     */
    public static String column(String column) {
        return quoteIfNeeded(current(), column);
    }

    /**
     * 渲染字段别名，规则同 {@link #column(String)}。
     *
     * @param alias 字段别名
     * @return 可直接拼入 SQL 的别名文本
     */
    public static String alias(String alias) {
        return quoteIfNeeded(current(), alias);
    }

    /**
     * 无条件引用别名，供需要保留驼峰大小写的 PostgreSQL / Oracle 别名使用。
     *
     * @param alias 字段别名
     * @return 引用后的别名
     */
    public static String aliasAlways(String alias) {
        if (alias == null || alias.isEmpty()) {
            return alias;
        }
        return current().quote(unwrap(alias));
    }

    /**
     * 取当前方言的标识符引用器，容器未就绪时退化为 ANSI。
     *
     * @return 引用器
     */
    public static IIdentifierQuoter current() {
        SqlDialect dialect = SpringUtil.getBeanSafe(SqlDialect.class, IGNORE);
        return dialect == null ? IdentifierQuoting.ANSI : dialect.getIdentifierQuoter();
    }

    /**
     * 按需引用：已带引用符的规范化为当前方言符号，保留字加引用，其余原样返回。
     * 限定名按点分段处理；函数、表达式等非标识符文本不受影响。
     *
     * @param quoter     方言引用器
     * @param identifier 标识符或表达式
     * @return 可直接拼入 SQL 的文本
     */
    public static String quoteIfNeeded(IIdentifierQuoter quoter, String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return identifier;
        }
        if (isQuoted(identifier)) {
            return quoter.quote(unwrap(identifier));
        }
        int dot = identifier.indexOf('.');
        if (dot < 0) {
            return quoteSegment(quoter, identifier);
        }
        StringBuilder builder = new StringBuilder(identifier.length() + 4);
        int start = 0;
        while (dot >= 0) {
            builder.append(quoteSegment(quoter, identifier.substring(start, dot))).append('.');
            start = dot + 1;
            dot = identifier.indexOf('.', start);
        }
        return builder.append(quoteSegment(quoter, identifier.substring(start))).toString();
    }

    private static String quoteSegment(IIdentifierQuoter quoter, String segment) {
        if (segment.isEmpty()) {
            return segment;
        }
        if (isQuoted(segment)) {
            return quoter.quote(unwrap(segment));
        }
        if (!RESERVED_WORDS.contains(segment.toUpperCase(Locale.ROOT))) {
            return segment;
        }
        return quoter.quote(segment);
    }

    private static boolean isQuoted(String identifier) {
        if (identifier == null || identifier.length() < 2) {
            return false;
        }
        char first = identifier.charAt(0);
        char last = identifier.charAt(identifier.length() - 1);
        return (first == '"' && last == '"') || (first == '`' && last == '`')
            || (first == '[' && last == ']');
    }

    private static String unwrap(String identifier) {
        return isQuoted(identifier) ? identifier.substring(1, identifier.length() - 1) : identifier;
    }
}
