package com.tbs.sql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 查询参数对象
 *
 * @author abstergo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FetchObject implements Serializable {
    /**
     * 查询对象
     */
    Object entity;
    /**
     * 分页参数
     */
    SqlLimitModel limitModel;
    /**
     * 查询条件
     */
    SqlWhereCompose whereCompose;
    /**
     * 排序参数
     */
    List<SqlOrderInfo> orderInfos;
    /**
     * 分组参数
     */
    List<SqlGroupModel> sqlGroupModels;

}
