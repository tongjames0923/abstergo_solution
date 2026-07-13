package com.tbs.shared.pojo;

public interface ICommonCreateAuditable<UK> extends ICreateTimeAuditable {

    /**
     * 创建者
     */
    void setCreateBy(UK creator);

    /**
     * 创建者
     */
    UK getCreateBy();
}
