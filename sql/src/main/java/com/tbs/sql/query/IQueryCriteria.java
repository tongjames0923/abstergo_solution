package com.tbs.sql.query;

import com.tbs.sql.framework.model.SqlSelectInfoModel;

import java.io.Serializable;

/**
 * @author abstergo
 */
public interface IQueryCriteria extends Serializable {

    /**
     * 获取查询SQL信息
     *
     * @return
     */
    SqlSelectInfoModel getSelectSQLInfo();

}
