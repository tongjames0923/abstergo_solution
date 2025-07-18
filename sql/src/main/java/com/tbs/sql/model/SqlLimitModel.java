package com.tbs.sql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * limit语句模型
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SqlLimitModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long offset;
    private Long count;

}
