package com.tbs.app.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 实体基类
 *
 * @param <K>
 * @author tongj
 */
public interface IBaseEntity<K> extends IIdentifiable<K>, IFlagable, Serializable {

    /**
     * 获取创建时间
     *
     * @return
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    Date getCreateTime();

    /**
     * 设置创建时间
     *
     * @param createTime
     */
    void setCreateTime(Date createTime);

    /**
     * 获取更新时间
     *
     * @return
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    Date getUpdateTime();

    /**
     * 设置更新时间
     *
     * @param updateTime
     */
    void setUpdateTime(Date updateTime);

}
