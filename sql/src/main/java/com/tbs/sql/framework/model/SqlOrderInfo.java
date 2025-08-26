package com.tbs.sql.framework.model;

import com.tbs.sql.enums.QueryOrderEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * sql排序信息
 *
 * @author tongj
 */
@Data
public class SqlOrderInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 排序字段
     */
    private String filedName;
    /**
     * 排序方式
     */
    private QueryOrderEnum order;
}
