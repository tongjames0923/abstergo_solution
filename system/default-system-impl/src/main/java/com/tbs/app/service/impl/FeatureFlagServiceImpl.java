package com.tbs.app.service.impl;

import com.tbs.app.constant.UserServiceFeatureFlagConstants;
import com.tbs.app.service.IFeatureFlagService;
import com.tbs.common.util.base.CollUtil;
import com.tbs.dic.enums.DataDicCompareEnum;
import com.tbs.dic.model.DicModel;
import com.tbs.dic.service.IDataDicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class FeatureFlagServiceImpl implements IFeatureFlagService {

    @DubboReference(lazy = true)
    private IDataDicService dicService;

    @Override
    public boolean isEnableValue(String flagName, String value) {
        try {
            var featureFlag = dicService.valueEquals("SYSTEM", flagName, value);
            if (Objects.equals(featureFlag, DataDicCompareEnum.EQUALS.getCode())) {
                return true;
            } else if (CollUtil.isIn(featureFlag, DataDicCompareEnum.NO_DIC.getCode(),
                DataDicCompareEnum.NO_ITEM.getCode())) {
                log.warn("不存在SYSTEM字典的{}字典项。", UserServiceFeatureFlagConstants.USER_NAME_PASSWORD_VALIDATOR);
            }
        } catch (Exception e) {
            log.error("获取用户名密码校验功能开关异常", e);
        }
        return false;
    }

    @Override
    public List<String> getAllFeatureFlags() {
        DicModel dicModel = null;
        try {
            var model = dicService.getDicByCode("SYSTEM", true);
            if (CollUtil.isEmpty(model)) {
                return Arrays.asList();
            }
            dicModel = CollUtil.getFirst(model);
        } catch (Exception e) {
            log.error("获取系统字典失败", e);
        }
        var items = dicModel.getItems();
        if (CollUtil.isEmpty(items)) {
            return Arrays.asList();
        }
        return items.stream().map(item -> item.getLabel()).collect(Collectors.toList());

    }
}
