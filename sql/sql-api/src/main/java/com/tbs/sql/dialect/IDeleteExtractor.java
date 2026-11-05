package com.tbs.sql.dialect;

/**
 * DELETE语句提取器接口，生成删除语句。
 *
 * <p>单表 DELETE 在四种主流数据库上语法一致，默认由 {@code CommonDeleteExtractor} 提供，
 * 各方言按需覆写。WHERE 条件由调用方按具体删除语义（主键、IN、实体条件）拼装后传入，
 * 本接口只负责语句骨架。</p>
 *
 * @author abstergo
 * @since 3.0.0
 */
public interface IDeleteExtractor {

    /**
     * 构建删除语句。
     *
     * @param tableName      表名
     * @param whereCondition WHERE条件，为空串表示不带WHERE
     * @return DELETE语句
     */
    String buildDelete(String tableName, String whereCondition);
}
