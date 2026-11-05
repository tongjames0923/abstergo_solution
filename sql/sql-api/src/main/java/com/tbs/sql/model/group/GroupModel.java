package com.tbs.sql.model.group;

import lombok.Data;

import java.io.Serializable;

/**
 * SQL分组模型
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class GroupModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 分组字段名称 */
    private String fieldName;
}
