package com.tbs.sql.model.where;

import com.tbs.sql.enums.ConnectorEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class WhereNode<T> {
    /** 节点的连接符 */
    private ConnectorEnum connector;
    /** 节点的值（可以是WhereInfo或WhereCompose类型） */
    private T value;

    public boolean isWhereInfo() {
        return value instanceof WhereInfo;
    }

    public boolean isCompose() {
        return value instanceof WhereCompose;
    }

    public static WhereNode<WhereCompose> createComposeNode(ConnectorEnum connector, WhereCompose value) {
        return new WhereNode<>(connector, value);
    }

    public static WhereNode<WhereInfo> createInfoNode(ConnectorEnum connector, WhereInfo value) {
        return new WhereNode<>(connector, value);
    }
}
