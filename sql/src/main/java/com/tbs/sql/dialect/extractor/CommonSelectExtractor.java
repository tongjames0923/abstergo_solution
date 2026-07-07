package com.tbs.sql.dialect.extractor;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.dialect.ISelectExtractor;
import com.tbs.sql.exception.SqlExtractionException;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.model.select.SelectItemModel;

/**
 * 通用SELECT语句提取器，适用于MySQL、PostgreSQL、SQL Server、Oracle等数据库。
 *
 * <p>所有主流关系型数据库的SELECT语法（字段列表、别名、FROM子句）完全一致，
 * 因此使用单一公共实现，避免各方言重复定义。</p>
 *
 * @author abstergo
 * @since 2.0.0
 * @see ISelectExtractor
 */
public class CommonSelectExtractor implements ISelectExtractor {

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
