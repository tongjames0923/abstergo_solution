package com.tbs.app.pojo.entity;

/**
 * 支持id化的实体
 *
 * @author tongj
 * @param <K>
 */
public interface IIdentifiable<K> {
    /**
     * 设置id
     *
     * @param id
     */
    void setId(K id);

    /**
     * 获取id
     *
     * @return
     */
    K getId();
}
