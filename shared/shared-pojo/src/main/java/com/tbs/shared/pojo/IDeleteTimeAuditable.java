package com.tbs.shared.pojo;

import java.util.Date;

public interface IDeleteTimeAuditable {
    /**
     * 删除时间
     *
     * @return
     */
    void setDeleteTime(Date deleteTime);

    /**
     * 删除时间
     *
     * @return
     */
    Date getDeleteTime();
}
