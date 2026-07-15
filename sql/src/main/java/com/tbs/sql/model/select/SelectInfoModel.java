package com.tbs.sql.model.select;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * SQL查询信息模型类，封装SQL查询的各种信息。
 *
 * <p>该类封装了SQL查询所需的所有信息，包括原始SQL语句、表名、字段列表和实体类。
 * 用于在SQL构建过程中传递和存储查询信息。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>存储原始SQL语句 {@link #getRawSql()}</li>
 *   <li>存储表名 {@link #getTableName()}</li>
 *   <li>存储字段列表 {@link #getFieldList()}</li>
 *   <li>存储实体类 {@link #getEntityClass()}</li>
 *   <li>提供静态工厂方法创建实例</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在SQL构建过程中传递查询信息</li>
 *   <li>存储从实体类提取的元数据</li>
 *   <li>在SQL提取器中使用</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建原始SQL查询信息
 * SelectInfoModel selectInfo = SelectInfoModel.rawSql("SELECT * FROM users");
 * 
 * // 创建表名查询信息
 * SelectInfoModel selectInfo = SelectInfoModel.tableName("users");
 * 
 * // 创建自定义查询信息
 * SelectInfoModel selectInfo = SelectInfoModel.custom("users", fieldList);
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>原始SQL和表名二选一使用</li>
 *   <li>字段列表可以为空</li>
 *   <li>支持序列化</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see SelectItemModel
 * @see com.tbs.sql.metadata.EntityMetadataExtractor
 */
@Data
@NoArgsConstructor
public class SelectInfoModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 原始SQL语句 */
    private String rawSql;
    /** 表名 */
    private String tableName;
    /** 表别名 */
    private String tableAlias;
    /** 字段列表 */
    List<SelectItemModel> fieldList;
    /** 实体类 */
    private Class<?> entityClass;

    public SelectInfoModel(String rawSql, String tableName, String tableAlias, List<SelectItemModel> fieldList, Class<?> entityClass) {
        this.rawSql = rawSql;
        this.tableName = tableName;
        this.tableAlias = tableAlias;
        this.fieldList = fieldList;
        this.entityClass = entityClass;
    }

    public static SelectInfoModel rawSql(String sql) {
        return new SelectInfoModel(sql, null, null, null, null);
    }

    public static SelectInfoModel tableName(String tableName) {
        return new SelectInfoModel(null, tableName, null, null, null);
    }

    public static SelectInfoModel tableNameWithAlias(String tableName, String tableAlias) {
        return new SelectInfoModel(null, tableName, tableAlias, null, null);
    }

    public static SelectInfoModel custom(String tableName, List<SelectItemModel> fieldList) {
        return new SelectInfoModel(null, tableName, null, fieldList, null);
    }
}
