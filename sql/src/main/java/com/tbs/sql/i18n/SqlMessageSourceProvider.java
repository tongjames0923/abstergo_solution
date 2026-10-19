package com.tbs.sql.i18n;

import com.tbs.common.i18n.AbstractMessageSourceProvider;

/**
 * SQL 模块消息源提供者。
 * <p>
 * 注册 sql 模块的消息资源文件 basename {@code messages-sql}
 * （对应 classpath 下 {@code messages-sql.properties} 与 {@code messages-sql_en_US.properties}），
 * 使 {@code datasource.intercept.*} 系列拒绝文案能被全局 MessageSource 解析。
 * 此前缺少本 provider，导致连接/SQL 拦截拒绝时只能回显原始 key。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class SqlMessageSourceProvider extends AbstractMessageSourceProvider {

    @Override
    public String getBasename() {
        return "messages-sql";
    }

    @Override
    public int getOrder() {
        // 中间件层：介于通用模块(-1000)与业务模块之间
        return -500;
    }
}
