package com.tbs.common.data;

import java.util.Collection;
import java.util.Optional;

/**
 * 数据持有者管理器
 *
 * @author abstergo
 */
public interface IDataHolderManager {
    /**
     * 获取数据持有者
     *
     * @param name
     * @return
     */
    Optional<IDataHolder> getDataHolder(String name);

    /**
     * 获取数据持有者名称
     *
     * @return
     */
    Collection<String> getDataHolderNames();

    /**
     * 注册数据持有者
     *
     * @param name
     * @param dataHolder
     */
    void registerDataHolder(String name, IDataHolder dataHolder);

    /**
     * 注销数据持有者
     *
     * @param name
     */
    void unregisterDataHolder(String name);

}
