package com.tbs.common.data.impl;

import com.tbs.common.data.IDataHolder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Spring 管理的数据持有者管理器实现类
 * <p>
 * 该类是AbstractDataHolderManager的实现，通过Spring的依赖注入机制管理数据持有者映射表。 使用@Resource注解自动注入所有已注册的IDataHolder实例，以Map的形式组织，
 * 其中键为Bean名称，值为对应的IDataHolder实例。该实现完全依赖Spring容器进行Bean的管理和注入。
 *
 * @author abstergo
 * @see AbstractDataHolderManager 抽象数据持有者管理器
 * @see IDataHolder 数据持有者接口
 * @see Map 映射接口
 * @see Resource 资源注入注解
 */
public class SpringManagedDataHolderManager extends AbstractDataHolderManager {

    /**
     * 数据持有者映射表
     * <p>
     * 通过Spring的@Resource注解自动注入，包含所有已注册的IDataHolder实例， 键为Bean名称，值为对应的IDataHolder实例。该映射表由Spring容器自动维护和管理。
     *
     * @see Resource 资源注入注解
     * @see IDataHolder 数据持有者接口
     * @see Map 映射接口
     */
    @Resource
    private Map<String, IDataHolder> dataHolderMap;

    /**
     * 获取数据持有者映射表
     * <p>
     * 实现AbstractDataHolderManager的抽象方法，返回由Spring注入的数据持有者映射表。 该映射表包含所有已注册的IDataHolder实例，键为Bean名称，值为对应的IDataHolder实例。
     *
     * @return 数据持有者映射表，键为Bean名称，值为IDataHolder实例
     * @see AbstractDataHolderManager#getDataHolderMap() 抽象数据持有者管理器获取映射表方法
     * @see Map 映射接口
     * @see IDataHolder 数据持有者接口
     * @see NotNull 非空注解
     */
    @Override
    protected @NotNull Map<String, IDataHolder> getDataHolderMap() {
        return dataHolderMap;
    }
}
