package com.tbs.shared.pojo;

import java.util.Date;

public interface ICommonTimeAuditable {

    /**
     * 创建时间
     *
     * @return 创建时间
     */
    Date getCreateTime();

    /**
     * 更新时间
     *
     * @return 更新时间
     */
    Date getUpdateTime();

    /**
     * 设置创建时间
     *
     * @param createTime
     */
    void setCreateTime(Date createTime);

    /**
     * 设置更新时间
     *
     * @param updateTime
     */
    void setUpdateTime(Date updateTime);
}
