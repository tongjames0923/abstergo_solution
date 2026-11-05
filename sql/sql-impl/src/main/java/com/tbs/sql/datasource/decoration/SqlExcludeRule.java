package com.tbs.sql.datasource.decoration;

import com.tbs.sql.util.SqlTextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SQL 排除规则：命中的 SQL 直接透传执行，跳过拦截器链。
 *
 * <p>规则来源为 {@code tbs.sql.exclude-sql} 配置的前缀列表（忽略大小写，
 * 匹配前先剥离 SQL 头部注释与空白），既是防误伤手段也是性能保护：
 * 批量任务、连接池健康检查等框架自身语句可借此短路。</p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SqlExcludeRule {

    /**
     * 归一化后的排除前缀列表
     */
    private final List<String> excludePrefixes;

    /**
     * 构造排除规则。
     *
     * @param excludePrefixes 排除前缀列表，null 或空列表表示不排除任何 SQL
     */
    public SqlExcludeRule(List<String> excludePrefixes) {
        List<String> normalized = new ArrayList<>();
        if (excludePrefixes != null) {
            for (String prefix : excludePrefixes) {
                if (prefix == null || prefix.trim().isEmpty()) {
                    continue;
                }
                normalized.add(prefix.trim().toLowerCase(java.util.Locale.ROOT));
            }
        }
        this.excludePrefixes = Collections.unmodifiableList(normalized);
    }

    /**
     * 判断 SQL 是否命中排除规则。
     *
     * @param sql 待执行 SQL，允许为 null
     * @return true 表示命中排除，应跳过拦截器链
     */
    public boolean excludes(String sql) {
        if (excludePrefixes.isEmpty() || sql == null) {
            return false;
        }
        String normalized = SqlTextUtils.normalize(sql);
        for (String prefix : excludePrefixes) {
            if (normalized.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否存在排除规则。
     *
     * @return true 表示配置了至少一条排除前缀
     */
    public boolean hasRules() {
        return !excludePrefixes.isEmpty();
    }
}
