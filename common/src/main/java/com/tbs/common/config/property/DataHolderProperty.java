package com.tbs.common.config.property;

import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderManager;
import com.tbs.common.data.impl.MapDataHolder;
import com.tbs.common.data.impl.SpringManagedDataHolderManager;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author abstergo
 */
@ConfigurationProperties(prefix = "tbs.data")
@Component
@Data
public class DataHolderProperty {
    /**
     * 数据持有者管理器
     */
    private Class<? extends IDataHolderManager> dataHolderManager = SpringManagedDataHolderManager.class;

    /**
     * 默认的数据持有者
     */
    private Class<? extends IDataHolder> dataHolder = MapDataHolder.class;

}
