package com.tbs.shared.pojo;

public interface ICommonDeleteAuditable<UK> extends IDeleteTimeAuditable {

    /**
     * 获取删除人
     *
     * @return
     */
    UK getDeleteBy();

    /**
     * 设置删除人
     *
     * @param deleteBy
     */
    void setDeleteBy(UK deleteBy);
}
