package com.tbs.sql.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 实体表元数据，描述实体与数据库表的完整映射关系。
 *
 * <p>供写操作（INSERT / UPDATE / DELETE）SQL 生成使用。与查询专用的
 * {@link com.tbs.sql.model.select.SelectInfoModel} 分离，避免查询模型的别名、原始 SQL
 * 等字段污染写操作语义。</p>
 *
 * <h3>能力边界</h3>
 * <ul>
 *   <li>只包含语句生成所需的表名、列清单与主键信息，不承载查询条件</li>
 *   <li>列清单顺序与实体字段声明顺序一致</li>
 * </ul>
 *
 * @author abstergo
 * @since 3.0.0
 * @see EntityColumnMetadata
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityTableMetadata {
    /** 表名 */
    private String tableName;
    /** 实体类型 */
    private Class<?> entityClass;
    /** 持久化列清单，不含 {@code @Transient} 与静态、final 字段 */
    private List<EntityColumnMetadata> columns;

    /**
     * 获取主键列。
     *
     * @return 主键列元数据，实体未标注 {@code @Id} 时返回 null
     */
    public EntityColumnMetadata getIdColumn() {
        if (columns == null) {
            return null;
        }
        for (EntityColumnMetadata column : columns) {
            if (column.isId()) {
                return column;
            }
        }
        return null;
    }
}
