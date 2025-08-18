package com.tbs.shared.pojo;

/**
 * 支持id化的实体
 *
 * @param <K>
 * @author tongj
 */
public interface IIdentifiable<K> {
    /**
     * 获取id
     *
     * @return
     */
    K getId();

    /**
     * 设置id
     *
     * @param id
     */
    void setId(K id);
}
