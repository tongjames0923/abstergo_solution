package com.tbs.sql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 查询字段信息
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SqlSelectItemModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private String fieldName;
    private String fieldAlias;
}
