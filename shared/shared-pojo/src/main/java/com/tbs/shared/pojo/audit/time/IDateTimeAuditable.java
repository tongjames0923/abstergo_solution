package com.tbs.shared.pojo.audit.time;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tbs.shared.pojo.audit.ITimeAuditable;

import java.util.Date;

/**
 * 时间审计接口
 *
 * @author abstergo
 */
public interface IDateTimeAuditable extends ITimeAuditable<Date> {
    
    Date getCreateTime();

    void setCreateTime(Date createTime);

    
    Date getUpdateTime();

    void setUpdateTime(Date updateTime);
}
