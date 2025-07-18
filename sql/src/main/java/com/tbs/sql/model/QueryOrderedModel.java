package com.tbs.sql.model;

import com.tbs.sql.enums.QueryOrderEnum;
import com.tbs.sql.order.IQueryOrderedModel;

/**
 * 动态排序模型的实现类，支持运行时修改排序
 * @author abstergo
 */
public class QueryOrderedModel implements IQueryOrderedModel {
    private QueryOrderEnum ordered;

    public QueryOrderedModel(QueryOrderEnum ordered) {
        this.ordered = ordered;
    }

    public QueryOrderedModel() {
    }

    public void setOrdered(QueryOrderEnum ordered) {
        this.ordered = ordered;
    }

    @Override
    public QueryOrderEnum getOrdered() {
        return ordered;
    }
}
