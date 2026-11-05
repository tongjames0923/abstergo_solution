package com.tbs.sql.model.order;

import com.tbs.sql.enums.OrderEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * SQL排序信息模型
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class OrderInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 排序字段 */
    private String fieldName;
    /** 排序方式 */
    private OrderEnum order;
}
