package com.tbs.sql.dialect.extractor;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.dialect.ISelectExtractor;
import com.tbs.sql.exception.SqlExtractionException;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.model.select.SelectItemModel;

import java.util.List;

/**
 * 通用SELECT语句提取器，适用于MySQL、PostgreSQL、SQL Server、Oracle等数据库。
 *
 * <p>所有主流关系型数据库的SELECT语法（字段列表、别名、FROM子句）完全一致，
 * 因此使用单一公共实现，避免各方言重复定义。</p>
 *
 * <p>实现说明：预分配 StringBuilder 一次成型，用 setLength 去掉末尾逗号，
 * 不再产生 substring 中间串，也不再走 String.format 的 Formatter。</p>
 *
 * @author abstergo
 * @since 2.0.0
 * @see ISelectExtractor
 */
public class CommonSelectExtractor implements ISelectExtractor {

    @Override
    public String extract(SelectInfoModel infoModel) {
        if (infoModel == null) {
            AssertUtil.throwError(() -> new SqlExtractionException("SelectInfoModel can not be null", this));
        }
        // 缓存以本提取器实例为 owner，天然隔离不同方言；命中时跳过全部渲染
        String cached = infoModel.cachedSelectSql(this);
        if (cached != null) return cached;
        String result = render(infoModel);
        infoModel.cacheSelectSql(this, result);
        return result;
    }

    private String render(SelectInfoModel infoModel) {
        // 断言改为「先判断、异常路径才构造 supplier」：正常路径不再为每个字段创建捕获 this 的 lambda
        if (StrUtil.isNotEmpty(infoModel.getRawSql())) return infoModel.getRawSql();
        if (StrUtil.isEmpty(infoModel.getTableName())) {
            AssertUtil.throwError(() -> new SqlExtractionException("tableName can not be null", this));
        }

        String fromClause = buildFromClause(infoModel.getTableName(), infoModel.getTableAlias());
        List<SelectItemModel> fields = infoModel.getFieldList();
        if (CollUtil.isEmpty(fields)) return "SELECT * FROM " + fromClause;

        StringBuilder sb = new StringBuilder(16 + fromClause.length() + fields.size() * 24);
        sb.append("SELECT ");
        for (SelectItemModel item : fields) {
            if (item == null) {
                AssertUtil.throwError(() -> new SqlExtractionException("SelectItemModel can not be null", this));
            }
            if (StrUtil.isEmpty(item.getFieldName())) {
                AssertUtil.throwError(() -> new SqlExtractionException("fieldName can not be null", this));
            }
            sb.append(item.getFieldName());
            if (StrUtil.isNotEmpty(item.getFieldAlias())) sb.append(" AS ").append(item.getFieldAlias());
            sb.append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.append(" FROM ").append(fromClause).toString();
    }

    /**
     * 构建FROM子句，支持表别名。
     *
     * @param tableName 表名
     * @param tableAlias 表别名，可以为null
     * @return FROM子句
     */
    private String buildFromClause(String tableName, String tableAlias) {
        if (StrUtil.isNotEmpty(tableAlias)) {
            return tableName + " AS " + tableAlias;
        }
        return tableName;
    }
}
