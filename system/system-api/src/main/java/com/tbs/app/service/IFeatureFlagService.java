package com.tbs.app.service;

import com.tbs.app.vo.SystemFeatureFlagVO;

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
     * 启用功能标识
     *
     * @param flagName
     */
    void enableFlag(String flagName);

    /**
     * 禁用功能标识
     *
     * @param flagName
     */
    void disableFlag(String flagName);

    /**
     * 添加系统功能标识
     *
     * @param systemFeatureFlagVO
     */
    void addSystemFeatureFlag(SystemFeatureFlagVO systemFeatureFlagVO);

    /**
     * 修改系统功能标识
     *
     * @param systemFeatureFlagVO
     */
    void updateSystemFeatureFlag(SystemFeatureFlagVO systemFeatureFlagVO);

    /**
     * 删除系统功能标识
     *
     * @param flagName
     */
    void deleteSystemFeatureFlag(String flagName);


    /**
     * 根据功能标识名称获取功能标识列表
     *
     * @param flagName
     * @return
     */
    List<SystemFeatureFlagVO> listAllSystemFeatureFlags();
}
