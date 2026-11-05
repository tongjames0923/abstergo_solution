package com.tbs.sql.dialect.extractor;

import com.tbs.sql.dialect.IDeleteExtractor;

/**
 * 通用DELETE语句提取器，生成标准删除语句。
 *
 * <p>单表 DELETE 在四种数据库语法一致，故各方言默认复用本实现。仅拼装语句骨架
 * {@code DELETE FROM 表 [WHERE 条件]}，具体 WHERE 条件由调用方按删除语义（主键、IN、实体条件）生成后传入。</p>
 *
 * @author abstergo
 * @since 3.0.0
 * @see IDeleteExtractor
 */
public class CommonDeleteExtractor implements IDeleteExtractor {

    @Override
    public String buildDelete(String tableName, String whereCondition) {
        String sql = "DELETE FROM " + tableName;
        return whereCondition == null || whereCondition.isEmpty() ? sql : sql + " WHERE " + whereCondition;
    }
}
