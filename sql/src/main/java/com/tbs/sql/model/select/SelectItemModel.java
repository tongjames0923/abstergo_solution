package com.tbs.sql.model.select;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * SQL查询字段模型类，封装SQL查询中的字段信息。
 *
 * <p>该类封装了SQL查询中的单个字段信息，包括字段名称和字段别名。
 * 用于在构建SELECT语句时传递字段信息。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>存储字段名称 {@link #getFieldName()}</li>
 *   <li>存储字段别名 {@link #getFieldAlias()}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>在构建SELECT语句时传递字段信息</li>
 *   <li>存储从实体类提取的字段映射</li>
 *   <li>在SQL提取器中使用</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建字段模型
 * SelectItemModel item = new SelectItemModel("user_name", "name");
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>字段别名可以为null</li>
 *   <li>支持序列化</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see SelectInfoModel
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectItemModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 字段名称 */
    private String fieldName;
    /** 字段别名 */
    private String fieldAlias;
}
