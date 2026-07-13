package com.tbs.shared.pojo;

import java.util.Date;

public interface ICreateTimeAuditable {
    /**
     * 创建时间
     */
    void setCreateTime(Date createTime);

    /**
     * 创建者
     */
    Date getCreateTime();
}
