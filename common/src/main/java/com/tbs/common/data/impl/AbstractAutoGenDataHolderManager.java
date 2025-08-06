package com.tbs.common.data.impl;

import com.tbs.common.constant.DataHolderBeanNameConstant;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderManager;
import com.tbs.common.error.AssertUtil;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 抽象自动生成数据管理器
 *
 * @author abstergo
 */
public abstract class AbstractAutoGenDataHolderManager implements IDataHolderManager {

    @Resource(name = DataHolderBeanNameConstant.DEFAULT_DATA_HOLDER)
    private IDataHolder defaultDataHolder;

    private Map<String, IDataHolder> dataHolders = new HashMap<>(8);

    /**
     * 获取数据管理器中的数据
     *
     * @param name
     * @param gobalDataHolder
     * @return
     */
    protected abstract IDataHolder getPartOfDataHolder(String name, IDataHolder gobalDataHolder);

    @Override
    public Optional<IDataHolder> getDataHolder(String name) {
        return Optional.ofNullable(dataHolders.computeIfAbsent(name, (k) -> {
            return getPartOfDataHolder(k, defaultDataHolder);
        }));
    }

    @Override
    public Collection<String> getDataHolderNames() {
        return dataHolders.keySet();
    }

    @Override
    public void registerDataHolder(String name, IDataHolder dataHolder) {
        AssertUtil.isTrue(dataHolders.containsKey(name), "dataHolder " + name + " already exists");
        dataHolders.put(name, dataHolder);
    }

    @Override
    public void unregisterDataHolder(String name) {
        AssertUtil.isTrue(!dataHolders.containsKey(name), "dataHolder " + name + " not exists");
        dataHolders.remove(name);
    }
}
