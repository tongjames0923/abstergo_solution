package com.tbs.shared.pojo;

public interface ICommonUpdateAuditable<UK> extends IUpdateTimeAuditable {
    /**
     * 获取更新者
     *
     * @return
     */
    UK getUpdateBy();

    /**
     * 设置更新者
     *
     * @param updateUser
     */
    void setUpdateBy(UK updateBy);

}
