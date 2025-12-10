package com.tbs.sql.framework.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL条件组合模型
 * <p>
 * 表示SQL WHERE子句中的条件组合，通常对应于一对括号中的所有SQL条件语句
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class SqlWhereCompose implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 括号中SQL where语句的集合
     */
    @Getter
    @Setter
    private List<SqlWhereNode> children = new ArrayList<>(32);

}
