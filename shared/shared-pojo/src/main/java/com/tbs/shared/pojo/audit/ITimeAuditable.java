package com.tbs.shared.pojo.audit;

/**
 * 时间审计接口
 *
 * @author abstergo
 */
public interface ITimeAuditable<TIME_TYPE> {
    /**
     * 创建时间
     *
     * @return
     */
    TIME_TYPE getCreateTime();

    /**
     * 设置创建时间
     *
     * @param createTime
     */
    void setCreateTime(TIME_TYPE createTime);

    /**
     * 更新时间
     *
     * @return
     */
    TIME_TYPE getUpdateTime();

    /**
     * 设置更新时间
     *
     * @param updateTime
     */
    void setUpdateTime(TIME_TYPE updateTime);

}
