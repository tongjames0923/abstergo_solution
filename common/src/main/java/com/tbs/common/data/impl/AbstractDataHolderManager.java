package com.tbs.common.data.impl;

import com.tbs.common.constant.DataHolderBeanNameConstant;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderManager;
import com.tbs.common.error.AssertUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 数据持有者管理抽象类
 *
 * @author jiangnan.jn
 * @date 2020/03/05
 */
public abstract class AbstractDataHolderManager implements IDataHolderManager {

    @Resource(name = DataHolderBeanNameConstant.DEFAULT_DATA_HOLDER)
    private IDataHolder defaultDataHolder;

    /**
     * 获取数据持有者映射表
     *
     * @return 数据持有者映射表
     */
    protected abstract @NotNull Map<String, IDataHolder> getDataHolderMap();

    @Override
    public Optional<IDataHolder> getDataHolder(String name) {
        return Optional.ofNullable(getDataHolderMap().get(name));
    }

    @Override
    public Collection<String> getDataHolderNames() {
        return getDataHolderMap().keySet();
    }

    @Override
    public void registerDataHolder(String name, IDataHolder dataHolder) {
        AssertUtil.isTrue(!getDataHolderMap().containsKey(name), "dataHolder " + name + " already exists");
        getDataHolderMap().put(name, dataHolder);
    }

    @Override
    public void unregisterDataHolder(String name) {
        AssertUtil.isTrue(!getDataHolderMap().containsKey(name), "dataHolder " + name + " not exists");
        getDataHolderMap().remove(name);
    }
}
