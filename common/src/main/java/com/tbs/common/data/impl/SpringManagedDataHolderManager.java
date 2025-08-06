package com.tbs.common.data.impl;

import com.tbs.common.data.IDataHolder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Spring 管理的 DataHolder 管理器
 *
 * @author abstergo
 */
public class SpringManagedDataHolderManager extends AbstractDataHolderManager {

    @Resource
    private Map<String, IDataHolder> dataHolderMap;

    @Override
    protected @NotNull Map<String, IDataHolder> getDataHolderMap() {
        return dataHolderMap;
    }
}
