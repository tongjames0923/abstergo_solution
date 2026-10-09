package com.tbs.common.config;

import com.tbs.common.config.property.DataHolderProperty;
import com.tbs.common.constant.DataHolderBeanNameConstant;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderManager;
import com.tbs.common.data.impl.MapDataHolder;
import com.tbs.common.data.impl.SpringManagedDataHolderManager;
import com.tbs.common.util.base.BeanUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

/**
 * @author abstergo
 */
@Configuration
public class DataHolderConfig {

    @Resource
    DataHolderProperty dataHolderProperty;

    @Bean
    @ConditionalOnMissingBean(IDataHolderManager.class)
    public IDataHolderManager dataHolderManager() {
        return BeanUtil.buildBeanFromProperties(SpringManagedDataHolderManager::new,
            dataHolderProperty.getDataHolderManager(), BeanUtil::buildByEmptyArgs);
    }

    @Bean(DataHolderBeanNameConstant.DEFAULT_DATA_HOLDER)
    IDataHolder defaultDataHolder() {
        return BeanUtil.buildBeanFromProperties(MapDataHolder::new, dataHolderProperty.getDataHolder(),
            BeanUtil::buildByEmptyArgs);
    }
}
