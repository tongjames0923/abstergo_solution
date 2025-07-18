package com.tbs.sql.model;

import lombok.Data;

import java.io.Serializable;

/**
 *
 * @author abstergo
 */
@Data
public class SqlGroupModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private String fieldName;
}
