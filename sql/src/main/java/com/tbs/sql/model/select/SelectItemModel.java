package com.tbs.sql.model.select;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * SQL查询字段模型
 *
 * @author abstergo
 * @since 1.0.0
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
