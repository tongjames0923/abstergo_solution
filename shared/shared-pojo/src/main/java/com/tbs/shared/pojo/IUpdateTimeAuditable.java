package com.tbs.shared.pojo;

import java.util.Date;

public interface IUpdateTimeAuditable {
    /**
     * 设置更新时间
     *
     * @param updateTime
     */
    void setUpdateTime(Date updateTime);

    /**
     * 获取更新时间
     *
     * @return
     */
    Date getUpdateTime();
}
