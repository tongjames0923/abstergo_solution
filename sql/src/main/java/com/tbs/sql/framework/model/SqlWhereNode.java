package com.tbs.sql.framework.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.tbs.sql.enums.QueryConnectorEnum;

/**
 * SQL WHERE条件节点模型
 * <p>
 * 封装SQL WHERE条件中的节点信息，包含连接符和值
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SqlWhereNode<T> {
    /**
     * 节点的连接符
     */
    private QueryConnectorEnum connector;
    /**
     * 节点的值
     * <p>可以是SqlWhereInfo或SqlWhereCompose类型</p>
     */
    private T value;

    /**
     * 判断当前节点是否是SqlWhereInfo类型
     *
     * @return 是否是SqlWhereInfo类型
     */
    public boolean isWhereInfo() {
        return value instanceof SqlWhereInfo;
    }

    /**
     * 判断当前节点是否是SqlWhereCompose类型
     *
     * @return 是否是SqlWhereCompose类型
     */
    public boolean isCompose() {
        return value instanceof SqlWhereCompose;
    }

    /**
     * 创建SqlWhereCompose节点
     *
     * @param connector 连接符
     * @param value SqlWhereCompose对象
     * @return SqlWhereCompose节点
     */
    public static SqlWhereNode<SqlWhereCompose> createComposeNode(QueryConnectorEnum connector, SqlWhereCompose value) {
        return new SqlWhereNode<>(connector, value);
    }

    /**
     * 创建SqlWhereInfo节点
     *
     * @param connector 连接符
     * @param value SqlWhereInfo对象
     * @return SqlWhereInfo节点
     */
    public static SqlWhereNode<SqlWhereInfo> createInfoNode(QueryConnectorEnum connector, SqlWhereInfo value) {
        return new SqlWhereNode<SqlWhereInfo>(connector, value);
    }
}
