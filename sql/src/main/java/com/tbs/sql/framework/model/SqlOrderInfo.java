package com.tbs.sql.framework.model;

import com.tbs.sql.enums.QueryOrderEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * SQL排序信息模型
 * <p>
 * 封装SQL ORDER BY子句中的排序信息，包括排序字段和排序方式
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Data
public class SqlOrderInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 排序字段
     * <p>SQL ORDER BY子句中的字段名称</p>
     */
    private String filedName;
    /**
     * 排序方式
     * <p>SQL ORDER BY子句中的排序方向</p>
     */
    private QueryOrderEnum order;
}
