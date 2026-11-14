package com.tbs.sql.util;

import java.util.Locale;

/**
 * SQL 文本归一化工具，供排除规则、命令类型推断与内置拦截器共用。
 *
 * <p>仅做头部注释剥离、关键字截取与轻量特征判断，不做完整语法解析。
 * 实现全部基于字符下标扫描，不依赖正则引擎：常见情形（无头部注释、
 * 无需改写的单行 SQL）直接返回入参本身，不产生任何中间对象。</p>
 *
 * <h3>行为约定</h3>
 * <ul>
 *   <li>头部空白口径与原正则 {@code \s} 一致：空格、制表、换行、回车、换页、纵向制表；
 *       {@link #firstKeyword(String)} 在此基础上继续按
 *       {@link Character#isWhitespace(char)} 跳过 Unicode 空白，保持旧行为。</li>
 *   <li>支持头部连续混合的行注释（{@code --}）与 C 风格块注释；
 *       行注释延伸到第一个换行或文本末尾，未闭合的块注释视为正文开头。</li>
 *   <li>{@link #containsWhere(String)} 在剥离头部注释后的区间上做大小写不敏感的词边界匹配，
 *       词边界口径与原 {@code \b}（ASCII 的 {@code \w}）一致，不再整串转小写。</li>
 *   <li>热路径的前缀判断请使用 {@link #startsWithIgnoreCase(String, String)}，
 *       避免 {@link #normalize(String)} 的全量小写化；后者仅为兼容保留。</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class SqlTextUtils {

    /** WHERE 关键字及长度，用于零分配的词边界扫描 */
    private static final String WHERE_KEYWORD = "where";
    private static final int WHERE_LENGTH = WHERE_KEYWORD.length();

    private SqlTextUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 返回 SQL 正文的起始下标：跳过头部空白以及其后连续的行注释、块注释。
     *
     * <p>这是全类的核心原语。调用方可基于该下标在原串上做 region 匹配、
     * 截取首个 token 或折叠空白，从而避免任何整串复制。</p>
     *
     * @param sql 原始 SQL，允许为 null
     * @return 正文起始下标；null 返回 0，全文皆为空白/注释时返回 {@code sql.length()}
     */
    public static int contentStart(String sql) {
        if (sql == null) {
            return 0;
        }
        final int length = sql.length();
        int i = 0;
        while (true) {
            while (i < length && isSpace(sql.charAt(i))) {
                i++;
            }
            if (i + 1 >= length) {
                return i;
            }
            char c = sql.charAt(i);
            if (c == '-' && sql.charAt(i + 1) == '-') {
                int newline = sql.indexOf('\n', i + 2);
                if (newline < 0) {
                    // 行注释延伸到文本末尾，没有正文（旧正则要求换行结尾，会误判为正文）
                    return length;
                }
                i = newline + 1;
                continue;
            }
            if (c == '/' && sql.charAt(i + 1) == '*') {
                int close = sql.indexOf("*/", i + 2);
                if (close < 0) {
                    // 未闭合块注释：当作正文开头，与旧实现「剥离前导空白后原样返回」一致
                    return i;
                }
                i = close + 2;
                continue;
            }
            return i;
        }
    }

    /**
     * 剥离 SQL 头部注释与空白，返回归一化后的文本（不改变大小写）。
     *
     * @param sql 原始 SQL，允许为 null
     * @return 归一化后的 SQL；无头部注释时直接返回入参本身（零分配）
     */
    public static String stripLeadingComments(String sql) {
        if (sql == null || sql.isEmpty()) {
            return "";
        }
        int start = contentStart(sql);
        if (start == 0) {
            return sql;
        }
        return sql.substring(start);
    }

    /**
     * 归一化并转小写，用于大小写不敏感的前缀匹配与特征判断。
     *
     * <p>注意：本方法会全量扫描并（通常）分配一个新字符串，属于兼容 API。
     * 热路径请改用 {@link #startsWithIgnoreCase(String, String)} 或
     * {@link #containsWhere(String)}。</p>
     *
     * @param sql 原始 SQL，允许为 null
     * @return 归一化后的小写文本，null 时返回空串
     */
    public static String normalize(String sql) {
        if (sql == null || sql.isEmpty()) {
            return "";
        }
        int start = contentStart(sql);
        String body = start == 0 ? sql : sql.substring(start);
        return body.toLowerCase(Locale.ROOT);
    }

    /**
     * 截取 SQL 首个关键字（小写），用于命令类型推断。
     *
     * <p>直接在原串上定位 token，仅对关键字本身生成小写字符串，不复制整条 SQL。</p>
     *
     * @param sql 原始 SQL，允许为 null
     * @return 首个关键字的小写形式，无关键字时返回空串
     */
    public static String firstKeyword(String sql) {
        if (sql == null || sql.isEmpty()) {
            return "";
        }
        final int length = sql.length();
        int i = contentStart(sql);
        while (i < length && Character.isWhitespace(sql.charAt(i))) {
            i++;
        }
        int start = i;
        while (i < length && Character.isLetterOrDigit(sql.charAt(i))) {
            i++;
        }
        if (i == start) {
            return "";
        }
        return sql.substring(start, i).toLowerCase(Locale.ROOT);
    }

    /**
     * 判断 SQL 正文是否以指定前缀开头（忽略大小写）。
     *
     * <p>剥离头部注释后仅比较前缀长度的字符，不生成小写副本；
     * 复杂度 O(头部注释长度 + 前缀长度)，与 SQL 总长无关。</p>
     *
     * @param sql            原始 SQL，允许为 null
     * @param lowerCasePrefix 小写形式的前缀（如 {@code "select"}），空前缀返回 false
     * @return true 表示命中
     */
    public static boolean startsWithIgnoreCase(String sql, String lowerCasePrefix) {
        if (sql == null || lowerCasePrefix == null || lowerCasePrefix.isEmpty()) {
            return false;
        }
        int start = contentStart(sql);
        return sql.regionMatches(true, start, lowerCasePrefix, 0, lowerCasePrefix.length());
    }

    /**
     * 判断 SQL 是否包含 WHERE 子句（大小写不敏感 + 词边界，跳过头部注释）。
     *
     * <p>在原串正文区间上逐字符扫描，仅当首字符为 w/W 时才做忽略大小写比较，
     * 并校验左右词边界。不创建 Matcher、不生成小写副本，全程零分配。</p>
     *
     * @param sql 原始 SQL，允许为 null
     * @return true 表示包含 WHERE
     */
    public static boolean containsWhere(String sql) {
        if (sql == null || sql.isEmpty()) {
            return false;
        }
        final int length = sql.length();
        final int last = length - WHERE_LENGTH;
        int i = contentStart(sql);
        while (i <= last) {
            char c = sql.charAt(i);
            if ((c == 'w' || c == 'W')
                    && sql.regionMatches(true, i, WHERE_KEYWORD, 0, WHERE_LENGTH)
                    && (i == 0 || !isWordChar(sql.charAt(i - 1)))
                    && (i + WHERE_LENGTH == length || !isWordChar(sql.charAt(i + WHERE_LENGTH)))) {
                return true;
            }
            i++;
        }
        return false;
    }

    /**
     * 将 SQL 压缩为单行：首尾空白按 {@code trim()} 语义去掉，
     * 中间连续空白折叠为单个空格。
     *
     * <p>先一次扫描判定是否需要改写：格式良好的单行 SQL 直接返回入参本身；
     * 仅需裁剪时用一次 substring；确需折叠时只分配一个 StringBuilder。</p>
     *
     * @param sql 原始 SQL，允许为 null
     * @return 单行 SQL，null 或空串时返回空串
     */
    public static String singleLine(String sql) {
        if (sql == null || sql.isEmpty()) {
            return "";
        }
        final int length = sql.length();
        int start = 0;
        int end = length;
        // 等价于 String.trim()：去掉首尾码值 <= ' ' 的字符
        while (start < end && sql.charAt(start) <= ' ') {
            start++;
        }
        while (end > start && sql.charAt(end - 1) <= ' ') {
            end--;
        }
        if (start == end) {
            return "";
        }
        boolean unchanged = true;
        for (int i = start; i < end; i++) {
            char c = sql.charAt(i);
            if (isSpace(c) && (c != ' ' || (i + 1 < end && isSpace(sql.charAt(i + 1))))) {
                unchanged = false;
                break;
            }
        }
        if (unchanged) {
            return start == 0 && end == length ? sql : sql.substring(start, end);
        }
        StringBuilder out = new StringBuilder(end - start);
        int i = start;
        while (i < end) {
            char c = sql.charAt(i);
            if (isSpace(c)) {
                out.append(' ');
                i++;
                while (i < end && isSpace(sql.charAt(i))) {
                    i++;
                }
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    /**
     * 与正则 {@code \s}（未开启 UNICODE_CHARACTER_CLASS）等价的空白判定。
     */
    private static boolean isSpace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f' || c == 0x0B;
    }

    /**
     * 与正则 {@code \w}（未开启 UNICODE_CHARACTER_CLASS）等价的词字符判定，
     * 用于复现原 {@code \bwhere\b} 的边界语义。
     */
    private static boolean isWordChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9') || c == '_';
    }
}
