package com.tbs.common.data.impl;

import com.tbs.common.constant.DataHolderBeanNameConstant;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderManager;
import com.tbs.common.error.AssertUtil;
import org.jetbrains.annotations.NotNull;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 数据持有者管理器抽象实现类
 * <p>
 * 该类是IDataHolderManager接口的抽象实现，提供了数据持有者管理的基本功能。 通过Map数据结构维护数据持有者的注册信息，支持数据持有者的注册、注销、获取等操作。
 * 子类需要实现getDataHolderMap()抽象方法来提供具体的数据持有者映射表实现。
 *
 * @author jiangnan.jn
 * @date 2020/03/05
 * @see IDataHolderManager 数据持有者管理器接口
 * @see IDataHolder 数据持有者接口
 * @see Map 映射接口
 * @see Optional 可选值包装类
 * @see Collection 集合接口
 */
public abstract class AbstractDataHolderManager implements IDataHolderManager {

    /**
     * 默认数据持有者实例
     * <p>
     * 通过Spring的@Resource注解注入，使用DataHolderBeanNameConstant中定义的默认名称进行注入。
     *
     * @see DataHolderBeanNameConstant#DEFAULT_DATA_HOLDER 默认数据持有者名称常量
     * @see Resource 资源注入注解
     */
    @Resource(name = DataHolderBeanNameConstant.DEFAULT_DATA_HOLDER)
    private IDataHolder defaultDataHolder;

    /**
     * 获取数据持有者映射表
     * <p>
     * 抽象方法，由子类实现，用于提供存储数据持有者注册信息的Map实例。 该Map的键为数据持有者名称，值为对应的数据持有者实例。
     *
     * @return 数据持有者映射表，键为名称，值为数据持有者实例
     * @see Map 映射接口
     * @see IDataHolder 数据持有者接口
     */
    protected abstract  Map<String, IDataHolder> getDataHolderMap();

    /**
     * 根据名称获取数据持有者
     * <p>
     * 实现IDataHolderManager接口的获取数据持有者方法。根据指定的名称从映射表中查找对应的数据持有者实例， 如果找到则返回包含该实例的Optional，否则返回空的Optional。
     *
     * @param name 数据持有者的名称，不能为null
     * @return 包含数据持有者的Optional实例，如果未找到则为空
     * @see IDataHolderManager#getDataHolder(String) 数据持有者管理器接口方法
     * @see Map#get(Object) Map获取方法
     * @see Optional#ofNullable(Object) 可选值包装类方法
     */
    @Override
    public Optional<IDataHolder> getDataHolder(String name) {
        return Optional.ofNullable(getDataHolderMap().get(name));
    }

    /**
     * 获取所有数据持有者的名称
     * <p>
     * 实现IDataHolderManager接口的获取数据持有者名称方法。返回映射表中所有数据持有者的名称集合。
     *
     * @return 包含所有数据持有者名称的集合
     * @see IDataHolderManager#getDataHolderNames() 数据持有者管理器接口方法
     * @see Map#keySet() Map键集合方法
     * @see Collection 集合接口
     */
    @Override
    public Collection<String> getDataHolderNames() {
        return getDataHolderMap().keySet();
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
        AssertUtil.isTrue(!getDataHolderMap().containsKey(name), "dataHolder " + name + " already exists");
        getDataHolderMap().put(name, dataHolder);
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
        AssertUtil.isTrue(!getDataHolderMap().containsKey(name), "dataHolder " + name + " not exists");
        getDataHolderMap().remove(name);
    }
}
