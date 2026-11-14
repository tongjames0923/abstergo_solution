package com.tbs.sql.datasource.model;

import com.tbs.sql.util.SqlTextUtils;

/**
 * SQL 命令类型枚举，按 SQL 文本的首个关键字推断语句类别。
 *
 * <p>推断仅截取 SQL 头部首个非空白、非注释 token，不解析完整语法，
 * 用于拦截器快速分流（如仅拦截 UPDATE / DELETE），不保证语义精确。</p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum SqlCommandKind {

    /**
     * 查询语句（SELECT / WITH ... SELECT）
     */
    SELECT,

    /**
     * 插入语句（INSERT / REPLACE）
     */
    INSERT,

    /**
     * 更新语句（UPDATE）
     */
    UPDATE,

    /**
     * 删除语句（DELETE / TRUNCATE）
     */
    DELETE,

    /**
     * 数据库定义语句（CREATE / ALTER / DROP / GRANT 等）
     */
    DDL,

    /**
     * 无法识别的其他语句
     */
    OTHER;

    /**
     * 根据 SQL 文本推断命令类型。
     *
     * @param sql SQL 文本，允许为 null
     * @return 推断出的命令类型，无法推断时返回 {@link #OTHER}
     */
    public static SqlCommandKind of(String sql) {
        String keyword = SqlTextUtils.firstKeyword(sql);
        if (keyword.isEmpty()) {
            return OTHER;
        }
        // firstKeyword 已返回 Locale.ROOT 小写形式，直接比较可省去一次字符串分配
        switch (keyword) {
            case "select":
            case "with":
            case "show":
            case "describe":
            case "desc":
            case "explain":
                return SELECT;
            case "insert":
            case "replace":
                return INSERT;
            case "update":
                return UPDATE;
            case "delete":
            case "truncate":
                return DELETE;
            case "create":
            case "alter":
            case "drop":
            case "grant":
            case "revoke":
            case "comment":
            case "rename":
                return DDL;
            default:
                return OTHER;
        }
    }
}
