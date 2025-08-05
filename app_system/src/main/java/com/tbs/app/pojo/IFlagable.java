package com.tbs.app.pojo;

/**
 * 状态标志接口
 *
 * @author wangyang
 */
public interface IFlagable {
    /**
     * 获取状态标志
     *
     * @return
     */
    Integer getFlag();

    /**
     * 设置状态标志
     *
     * @param flag
     */
    void setFlag(Integer flag);
}
