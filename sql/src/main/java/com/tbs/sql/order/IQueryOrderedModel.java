package com.tbs.sql.order;

import com.tbs.sql.enums.QueryOrderEnum;

/**
 * Sql排序模型接口
 * @author abstergo
 */
public interface IQueryOrderedModel {

    /**
     * 获取排序方式
     *
     * @return
     */
    public QueryOrderEnum getOrdered();
}
