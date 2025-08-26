package com.tbs.sql.framework.extractor.impls.mysql;

import com.tbs.common.util.CollUtil;
import com.tbs.common.util.StrUtil;
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
        if (infoModel == null) {
            throw new SqlExtractionException("SqlSelectInfoModel can not be null", this);
        }
        if (StrUtil.isNotEmpty(infoModel.getRawSql())) {
            return infoModel.getRawSql();
        }
        if (StrUtil.isEmpty(infoModel.getTableName())) {
            throw new SqlExtractionException("tableName can not be null", this);
        }
        if (CollUtil.isEmpty(infoModel.getFieldList())) {
            return "SELECT * FROM " + infoModel.getTableName();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        for (SqlSelectItemModel sqlSelectItemModel : infoModel.getFieldList()) {
            if (StrUtil.isEmpty(sqlSelectItemModel.getFieldName())) {
                throw new SqlExtractionException("fieldName can not be null", this);
            }
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
