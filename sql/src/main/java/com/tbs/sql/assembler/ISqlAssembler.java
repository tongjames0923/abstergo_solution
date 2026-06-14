package com.tbs.sql.assembler;

/**
 * SQL组装器接口
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ISqlAssembler {
    String assembleSelectSql(String selectSql, String whereSql, String orderSql, String groupSql, String limitSql);
}
