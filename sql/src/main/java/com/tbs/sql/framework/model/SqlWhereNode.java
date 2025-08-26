package com.tbs.sql.framework.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.tbs.sql.enums.QueryConnectorEnum;

/**
 * @author abstergo
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
     */
    private T value;

    /**
     * 判断当前节点是否是SqlWhereInfo
     *
     * @return
     */
    public boolean isWhereInfo() {
        return value instanceof SqlWhereInfo;
    }

    /**
     * 节点是否是SqlWhereCompose
     *
     * @return
     */
    public boolean isCompose() {
        return value instanceof SqlWhereCompose;
    }

    /**
     * 创建SqlWhereCompose节点
     *
     * @param connector
     * @param value
     * @return
     */
    public static SqlWhereNode<SqlWhereCompose> createComposeNode(QueryConnectorEnum connector, SqlWhereCompose value) {
        return new SqlWhereNode<>(connector, value);
    }

    /**
     * 创建SqlWhereInfo节点
     *
     * @param connector
     * @param value
     * @return
     */
    public static SqlWhereNode<SqlWhereInfo> createInfoNode(QueryConnectorEnum connector, SqlWhereInfo value) {
        return new SqlWhereNode<SqlWhereInfo>(connector, value);
    }
}
