package com.tbs.shared.pojo.audit;

/**
 * 操作人审计
 */
public interface IOperatorAuditable<K> {

    /**
     * 获取创建人
     *
     * @return
     */
    K getCreatorId();

    /**
     * 获取创建人
     *
     * @param creatorId
     */
    void setCreatorId(K creatorId);

    /**
     * 获取创建人
     *
     * @return
     */
    K getUpdaterId();

    /**
     * 设置更新人
     *
     * @param updaterId
     */
    void setUpdaterId(K updaterId);

}
