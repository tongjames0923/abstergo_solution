package com.tbs.sql.dialect.postgresql.extractor;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.dialect.ISelectExtractor;
import com.tbs.sql.exception.SqlExtractionException;
import com.tbs.sql.model.select.SelectInfoModel;
import com.tbs.sql.model.select.SelectItemModel;

import java.util.List;

/**
 * PostgreSQL SELECT 语句提取器。
 *
 * <p>与 {@link com.tbs.sql.dialect.extractor.CommonSelectExtractor} 逻辑一致，但**对字段别名加双引号**，
 * 以规避 PostgreSQL 把未加引号的标识符（含 SELECT 别名）统一折叠为小写的特性。</p>
 *
 * <h3>为什么需要单独实现</h3>
 * <p>通用提取器生成 {@code user_name AS userName} 这类驼峰别名。在 MySQL 中结果集列名保留为
 * {@code userName}，MyBatis 可按实体属性名 {@code userName} 正常映射；但 PostgreSQL 会把未加引号的
 * 别名折叠成小写 {@code username}，导致驼峰属性（userName/nickName/createTime/deptId 等）无法映射、
 * 字段值为 {@code null}，表现为「SQL 能查出数据、应用却拿到残缺对象」。</p>
 *
 * <p>本类改为生成 {@code user_name AS "userName"}：双引号让 PostgreSQL 保留别名原大小写，结果集列名
 * 仍为 {@code userName}，与 Java 实体属性精确对应，映射恢复正常。MySQL 仍使用 {@link
 * com.tbs.sql.dialect.extractor.CommonSelectExtractor}（不加引号），互不影响。</p>
 *
 * @author abstergo
 * @since 2.0.0
 * @see com.tbs.sql.dialect.extractor.CommonSelectExtractor
 * @see ISelectExtractor
 */
public class PostgreSqlSelectExtractor implements ISelectExtractor {

    @Override
    public String extract(SelectInfoModel infoModel) {
        // 断言改为「先判断、异常路径才构造 supplier」：正常路径不再为每个字段创建捕获 this 的 lambda
        if (infoModel == null) {
            AssertUtil.throwError(() -> new SqlExtractionException("SelectInfoModel can not be null", this));
        }
        if (StrUtil.isNotEmpty(infoModel.getRawSql())) return infoModel.getRawSql();
        if (StrUtil.isEmpty(infoModel.getTableName())) {
            AssertUtil.throwError(() -> new SqlExtractionException("tableName can not be null", this));
        }

        String fromClause = buildFromClause(infoModel.getTableName(), infoModel.getTableAlias());
        List<SelectItemModel> fields = infoModel.getFieldList();
        if (CollUtil.isEmpty(fields)) return "SELECT * FROM " + fromClause;

        StringBuilder sb = new StringBuilder(16 + fromClause.length() + fields.size() * 26);
        sb.append("SELECT ");
        for (SelectItemModel item : fields) {
            if (item == null) {
                AssertUtil.throwError(() -> new SqlExtractionException("SelectItemModel can not be null", this));
            }
            if (StrUtil.isEmpty(item.getFieldName())) {
                AssertUtil.throwError(() -> new SqlExtractionException("fieldName can not be null", this));
            }
            sb.append(item.getFieldName());
            // 关键差异：别名加双引号，防止 PostgreSQL 折叠为小写
            if (StrUtil.isNotEmpty(item.getFieldAlias())) {
                sb.append(" AS \"").append(item.getFieldAlias()).append('"');
            }
            sb.append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.append(" FROM ").append(fromClause).toString();
    }

    /**
     * 构建 FROM 子句，支持表别名。
     *
     * @param tableName 表名
     * @param tableAlias 表别名，可以为 null
     * @return FROM 子句
     */
    private String buildFromClause(String tableName, String tableAlias) {
        if (StrUtil.isNotEmpty(tableAlias)) {
            return tableName + " AS " + tableAlias;
        }
        return tableName;
    }
}
