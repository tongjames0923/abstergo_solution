package com.tbs.app.service;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.pojo.IIdentifiable;
import com.tbs.sql.mapper.IFrameworkMapper;
import lombok.Getter;

import javax.annotation.Resource;

/**
 * 包含系统设置的功能服务模版
 *
 * @author abstergo
 */
public abstract class AbstractSystemSettingFeatureServiceTemplate<T extends IIdentifiable<K>, K, M extends IFrameworkMapper<T, K>>
    extends AbstractFullFeatureServiceTemplate<T, K, M> {

    @Resource
    @Getter
    private AppSettingProperty property;

}
