package com.tbs.sql.model.limit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * SQL分页模型
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LimitModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 偏移量 */
    private Long offset;
    /** 获取数量 */
    private Long count;
}
