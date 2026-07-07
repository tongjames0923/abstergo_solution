package com.tbs.sql.dialect.oracle.extractor;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.dialect.ISelectExtractor;
import com.tbs.sql.exception.SqlExtractionException;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.model.select.SelectItemModel;

/**
 * Oracle SELECT语句提取器。
 *
 * <p>该类实现了 {@link ISelectExtractor} 接口，提供了Oracle特有的SELECT语句提取功能。
 * Oracle的SELECT语法与MySQL/SQL Server基本一致，支持表名、字段列表、字段别名等。</p>
 *
 * @author abstergo
 * @since 2.0.0
 * @see ISelectExtractor
 * @see SelectInfoModel
 */
public class OracleSelectExtractor implements ISelectExtractor {

    @Override
    public String extract(SelectInfoModel infoModel) {
        AssertUtil.notNull(infoModel, () -> new SqlExtractionException("SelectInfoModel can not be null", this));
        if (StrUtil.isNotEmpty(infoModel.getRawSql())) return infoModel.getRawSql();
        AssertUtil.isTrue(StrUtil.isEmpty(infoModel.getTableName()),
            () -> new SqlExtractionException("tableName can not be null", this));
        if (CollUtil.isEmpty(infoModel.getFieldList())) return "SELECT * FROM " + infoModel.getTableName();
        StringBuilder sb = new StringBuilder("SELECT ");
        for (SelectItemModel item : infoModel.getFieldList()) {
            AssertUtil.notNull(item, () -> new SqlExtractionException("SelectItemModel can not be null", this));
            AssertUtil.isTrue(StrUtil.isEmpty(item.getFieldName()),
                () -> new SqlExtractionException("fieldName can not be null", this));
            sb.append(item.getFieldName());
            if (StrUtil.isNotEmpty(item.getFieldAlias())) sb.append(" AS ").append(item.getFieldAlias());
            sb.append(",");
        }
        String sql = sb.substring(0, sb.length() - 1);
        return String.format("%s FROM %s", sql, infoModel.getTableName());
    }
}
