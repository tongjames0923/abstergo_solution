package com.tbs.app.service;

import java.util.List;

public interface IFeatureFlagService {

    /**
     * 获取功能标识的值
     *
     * @param flagName
     * @return
     */
    boolean isEnableValue(String flagName, String value);

    /**
     * 获取所有功能标识
     *
     * @return
     */
    List<String> getAllFeatureFlags();
}
