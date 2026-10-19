package com.tbs.sql.datasource.decoration;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL 文本归一化工具，供排除规则、命令类型推断与内置拦截器共用。
 *
 * <p>仅做头部注释剥离、关键字截取与轻量特征判断，不做完整语法解析，
 * 保证每条 SQL 的处理开销为 O(头部长度)。</p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class SqlTextUtils {

    /**
     * 匹配头部连续行注释（-- ...）与块注释（/* ... *&#47;）
     */
    private static final Pattern LEADING_COMMENT_PATTERN =
            Pattern.compile("^(?:--[^\\n]*\\n|/\\*.*?\\*/|\\s)+", Pattern.DOTALL);

    /**
     * 匹配 WHERE 关键字（词边界匹配）
     */
    private static final Pattern WHERE_PATTERN = Pattern.compile("\\bwhere\\b");

    private SqlTextUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 剥离 SQL 头部注释与空白，返回归一化后的文本（不改变大小写）。
     *
     * @param sql 原始 SQL，允许为 null
     * @return 归一化后的 SQL，null 或空串时返回空串
     */
    public static String stripLeadingComments(String sql) {
        if (sql == null || sql.isEmpty()) {
            return "";
        }
        Matcher matcher = LEADING_COMMENT_PATTERN.matcher(sql);
        return matcher.replaceFirst("");
    }

    /**
     * 归一化并转小写，用于大小写不敏感的前缀匹配与特征判断。
     *
     * @param sql 原始 SQL，允许为 null
     * @return 归一化后的小写文本，null 时返回空串
     */
    public static String normalize(String sql) {
        return stripLeadingComments(sql).toLowerCase(Locale.ROOT);
    }

    /**
     * 截取 SQL 首个关键字（小写），用于命令类型推断。
     *
     * @param sql 原始 SQL，允许为 null
     * @return 首个关键字的小写形式，无关键字时返回空串
     */
    public static String firstKeyword(String sql) {
        String normalized = stripLeadingComments(sql);
        int end = 0;
        int length = normalized.length();
        while (end < length && Character.isWhitespace(normalized.charAt(end))) {
            end++;
        }
        int start = end;
        while (end < length && Character.isLetterOrDigit(normalized.charAt(end))) {
            end++;
        }
        return normalized.substring(start, end).toLowerCase(Locale.ROOT);
    }

    /**
     * 判断 SQL 是否包含 WHERE 子句（词边界匹配，剥离头部注释后的小写文本上执行）。
     *
     * @param sql 原始 SQL，允许为 null
     * @return true 表示包含 WHERE
     */
    public static boolean containsWhere(String sql) {
        return WHERE_PATTERN.matcher(normalize(sql)).find();
    }
}
