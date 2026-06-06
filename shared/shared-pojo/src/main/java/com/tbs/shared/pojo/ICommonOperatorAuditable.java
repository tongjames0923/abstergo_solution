package com.tbs.shared.pojo;

/**
 * 创建者、更新者审计接口
 *
 * @author abstergo
 * @param <K> 主键类型
 */
public interface ICommonOperatorAuditable<K> {

    /**
     * 创建者ID
     *
     * @return 创建者ID
     */
    K getCreatorId();

    /**
     * 设置创建者ID
     *
     * @param creatorId 创建者ID
     */
    void setCreatorId(K creatorId);

    /**
     * 更新者ID
     *
     * @return 更新者ID
     */
    K getUpdaterId();

    /**
     * 设置更新者ID
     *
     * @param updaterId 更新者ID
     */
    void setUpdaterId(K updaterId);

}
