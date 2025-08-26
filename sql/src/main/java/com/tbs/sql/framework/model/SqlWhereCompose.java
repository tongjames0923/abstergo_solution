package com.tbs.sql.framework.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Sql数据集合，通常对应的是一个括号中所有的SQL where语句
 *
 * @author tongj
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
