package com.tbs.sql.query;

import com.tbs.sql.model.SqlSelectInfoModel;

import java.io.Serializable;

/**
 * @author abstergo
 */
public interface IQuery extends Serializable {

    /**
     * 获取查询SQL信息
     *
     * @return
     */
    SqlSelectInfoModel getSelectSQLInfo();

}
