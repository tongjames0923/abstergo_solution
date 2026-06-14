package com.tbs.sql.criteria;

import com.tbs.sql.model.select.SelectInfoModel;

import java.io.Serializable;

/**
 * 查询条件接口
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IQueryCriteria extends Serializable {
    /**
     * 获取查询SQL信息
     *
     * @return SelectInfoModel
     */
    SelectInfoModel getSelectSQLInfo();
}
