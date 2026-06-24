package com.tbs.app.service.impl;

import com.tbs.app.service.IFeatureFlagService;
import com.tbs.common.util.base.CollUtil;
import com.tbs.dic.model.DicItemModel;
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
        DicModel dicModel = null;
        try {
            var model = dicService.getDicByCode("SYSTEM", true);
            if (CollUtil.isEmpty(model)) {
                log.warn("未找到系统数据字典");
                return false;
            }
            dicModel = CollUtil.getFirst(model);
        } catch (Exception e) {
            log.error("获取系统字典失败", e);
        }
        if (dicModel == null) {
            log.warn("未找到系统数据字典");
            return false;
        }
        var items = dicModel.getItems();
        if (CollUtil.isEmpty(items)) {
            log.warn("未找到系统数据字典项");
            return false;
        }
        var map = items.stream().collect(Collectors.toMap(DicItemModel::getLabel, DicItemModel::getValue));
        if (!map.containsKey(flagName)) {
            log.warn("未找到系统数据字典项:{}", flagName);
            return false;
        }
        return Objects.equals(value, map.get(flagName));
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
