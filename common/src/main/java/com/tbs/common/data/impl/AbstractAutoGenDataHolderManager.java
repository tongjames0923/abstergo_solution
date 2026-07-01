package com.tbs.common.data.impl;

import com.tbs.common.constant.DataHolderBeanNameConstant;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderManager;
import com.tbs.common.error.AssertUtil;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象自动生成数据持有者管理器实现类
 * <p>
 * 该类是IDataHolderManager接口的抽象实现，提供了自动创建数据持有者的功能。 当请求一个不存在的数据持有者时，会自动调用抽象方法生成新的数据持有者实例并缓存起来。
 * 使用HashMap作为数据持有者的存储结构，并通过computeIfAbsent方法实现按需创建的机制。
 *
 * @author abstergo
 * @see IDataHolderManager 数据持有者管理器接口
 * @see IDataHolder 数据持有者接口
 * @see Map 映射接口
 * @see HashMap 哈希映射实现
 * @see Optional 可选值包装类
 */
public abstract class AbstractAutoGenDataHolderManager implements IDataHolderManager {

    /**
     * 默认数据持有者实例
     * <p>
     * 通过Spring的@Resource注解注入，使用DataHolderBeanNameConstant中定义的默认名称进行注入。 作为全局数据持有者，用于生成部分数据持有者的基底数据源。
     *
     * @see DataHolderBeanNameConstant#DEFAULT_DATA_HOLDER 默认数据持有者名称常量
     * @see Resource 资源注入注解
     */
    @Resource(name = DataHolderBeanNameConstant.DEFAULT_DATA_HOLDER)
    private IDataHolder defaultDataHolder;

    /**
     * 数据持有者映射表
     * <p>
     * 用于存储已创建的数据持有者实例，键为数据持有者名称，值为对应的数据持有者实例。 使用HashMap实现，初始容量为8，支持按需创建和缓存数据持有者实例。
     */
    private Map<String, IDataHolder> dataHolders = new ConcurrentHashMap<>(8);

    /**
     * 获取部分数据持有者
     * <p>
     * 抽象方法，由子类实现，用于根据名称和全局数据持有者创建特定的数据持有者实例。 当请求的数据持有者不存在时，会调用此方法创建新的实例。
     *
     * @param name            数据持有者名称
     * @param gobalDataHolder 全局数据持有者，作为数据源
     * @return 新创建的数据持有者实例
     * @see IDataHolder 数据持有者接口
     */
    protected abstract IDataHolder getPartOfDataHolder(String name, IDataHolder gobalDataHolder);

    /**
     * 根据名称获取数据持有者
     * <p>
     * 实现IDataHolderManager接口的获取数据持有者方法。如果指定名称的数据持有者已存在则直接返回， 否则调用getPartOfDataHolder方法创建新的数据持有者实例并缓存。
     *
     * @param name 数据持有者的名称，不能为null
     * @return 包含数据持有者的Optional实例，不会为空
     * @see IDataHolderManager#getDataHolder(String) 数据持有者管理器接口方法
     * @see Map#computeIfAbsent(Object, java.util.function.Function) Map按需计算方法
     * @see Optional#ofNullable(Object) 可选值包装类方法
     */
    @Override
    public Optional<IDataHolder> getDataHolder(String name) {
        return Optional.ofNullable(dataHolders.computeIfAbsent(name, (k) -> {
            return getPartOfDataHolder(k, defaultDataHolder);
        }));
    }

    /**
     * 获取所有数据持有者的名称
     * <p>
     * 实现IDataHolderManager接口的获取数据持有者名称方法。返回映射表中所有已创建的数据持有者的名称集合。
     *
     * @return 包含所有数据持有者名称的集合
     * @see IDataHolderManager#getDataHolderNames() 数据持有者管理器接口方法
     * @see Map#keySet() Map键集合方法
     * @see Collection 集合接口
     */
    @Override
    public Collection<String> getDataHolderNames() {
        return dataHolders.keySet();
    }

    /**
     * 注册数据持有者
     * <p>
     * 实现IDataHolderManager接口的注册数据持有者方法。将指定的数据持有者实例以给定名称注册到映射表中。 如果指定名称已存在，则抛出异常。
     *
     * @param name       数据持有者的名称，不能为null
     * @param dataHolder 要注册的数据持有者实例，不能为null
     * @throws RuntimeException 当指定名称已存在时抛出
     * @see IDataHolderManager#registerDataHolder(String, IDataHolder) 数据持有者管理器接口方法
     * @see Map#containsKey(Object) Map键包含检查方法
     * @see Map#put(Object, Object) Map存储方法
     * @see AssertUtil 断言工具类
     */
    @Override
    public void registerDataHolder(String name, IDataHolder dataHolder) {
        AssertUtil.isTrue(dataHolders.containsKey(name), "dataHolder " + name + " already exists");
        dataHolders.put(name, dataHolder);
    }

    /**
     * 注销数据持有者
     * <p>
     * 实现IDataHolderManager接口的注销数据持有者方法。根据指定的名称从映射表中移除对应的数据持有者实例。 如果指定名称不存在，则抛出异常。
     *
     * @param name 要注销的数据持有者的名称，不能为null
     * @throws RuntimeException 当指定名称不存在时抛出
     * @see IDataHolderManager#unregisterDataHolder(String) 数据持有者管理器接口方法
     * @see Map#containsKey(Object) Map键包含检查方法
     * @see Map#remove(Object) Map删除方法
     * @see AssertUtil 断言工具类
     */
    @Override
    public void unregisterDataHolder(String name) {
        AssertUtil.isTrue(!dataHolders.containsKey(name), "dataHolder " + name + " not exists");
        dataHolders.remove(name);
    }
}
