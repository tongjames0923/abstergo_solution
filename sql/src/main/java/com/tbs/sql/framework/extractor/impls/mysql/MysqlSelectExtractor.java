package com.tbs.sql.framework.extractor.impls.mysql;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.exception.SqlExtractionException;
import com.tbs.sql.framework.extractor.ISelectSqlExtractor;
import com.tbs.sql.framework.model.SqlSelectInfoModel;
import com.tbs.sql.framework.model.SqlSelectItemModel;

/**
 * mysql select sql extractor
 *
 * @author abstergo
 */
public class MysqlSelectExtractor implements ISelectSqlExtractor {
    @Override
    public String extractSelectSql(SqlSelectInfoModel infoModel) {
        AssertUtil.notNull(infoModel, () -> new SqlExtractionException("SqlSelectInfoModel can not be null", this));
        if (StrUtil.isNotEmpty(infoModel.getRawSql())) {
            return infoModel.getRawSql();
        }
        AssertUtil.isTrue(StrUtil.isEmpty(infoModel.getTableName()),
            () -> new SqlExtractionException("tableName can not be null", this));
        if (CollUtil.isEmpty(infoModel.getFieldList())) {
            return "SELECT * FROM " + infoModel.getTableName();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        for (SqlSelectItemModel sqlSelectItemModel : infoModel.getFieldList()) {
            AssertUtil.notNull(sqlSelectItemModel,
                () -> new SqlExtractionException("SqlSelectItemModel can not be null", this));
            AssertUtil.isTrue(StrUtil.isEmpty(sqlSelectItemModel.getFieldName()),
                () -> new SqlExtractionException("fieldName can not be null", this));

            stringBuilder.append(sqlSelectItemModel.getFieldName());
            if (StrUtil.isNotEmpty(sqlSelectItemModel.getFieldAlias())) {
                stringBuilder.append(" AS ");
                stringBuilder.append(sqlSelectItemModel.getFieldAlias());
            }
            stringBuilder.append(",");
        }
        String sql = stringBuilder.substring(0, stringBuilder.length() - 1);
        return String.format("%s FROM %s", sql, infoModel.getTableName());
    }
}
