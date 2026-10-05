package com.tbs.app.service.impl;

import com.tbs.app.service.IFeatureFlagService;
import com.tbs.app.vo.SystemFeatureFlagVO;
import com.tbs.common.provider.IObjectProvider;
import com.tbs.common.util.base.CollUtil;
import com.tbs.dic.entity.SystemDataDicItemEntity;
import com.tbs.dic.enums.DataDicFlagEnum;
import com.tbs.dic.model.DicItemModel;
import com.tbs.dic.model.DicModel;
import com.tbs.dic.service.IDataDicService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class FeatureFlagServiceImpl implements IFeatureFlagService {

    @Resource
    private IObjectProvider objectProvider;

    /**
     * 解析数据字典服务：本地无实现时经 Dubbo 集群引用兜底
     *
     * @return 数据字典服务实例
     */
    private IDataDicService getDicService() {
        return objectProvider.getObject(IDataDicService.class);
    }

    @Override
    public boolean isEnableValue(String flagName, String value) {
        DicModel dicModel = getDicModel();
        if (dicModel == null) {
            log.warn("未找到系统数据字典");
            return false;
        }
        var items = dicModel.getItems();
        if (CollUtil.isEmpty(items)) {
            log.warn("未找到系统数据字典项");
            return false;
        }
        var map = items.stream().filter(i -> i != null && Objects.equals(i.getFlag(), DataDicFlagEnum.ENABLE.getCode()))
            .collect(Collectors.toMap(DicItemModel::getLabel, DicItemModel::getValue));
        if (!map.containsKey(flagName)) {
            log.warn("未找到系统数据字典项:{}", flagName);
            return false;
        }
        return Objects.equals(value, map.get(flagName));
    }

    private @Nullable DicModel getDicModel() {
        DicModel dicModel = null;
        try {
            var model = getDicService().getDicByCode("SYSTEM", true);
            if (CollUtil.isEmpty(model)) {
                log.warn("未找到系统数据字典");
                return null;
            }
            dicModel = CollUtil.getFirst(model);
        } catch (Exception e) {
            log.error("获取系统字典失败", e);
        }
        return dicModel;
    }

    @Override
    public void enableFlag(String flagName) {
        var dicModel = getDicModel();
        if (dicModel == null) {
            log.warn("未找到系统数据字典");
            return;
        }
        getDicService().enableDataDicItem("SYSTEM", flagName);
    }

    @Override
    public void disableFlag(String flagName) {
        var dicModel = getDicModel();
        if (dicModel == null) {
            log.warn("未找到系统数据字典");
            return;
        }
        getDicService().disableDataDicItem("SYSTEM", flagName);
    }

    @Override
    public void addSystemFeatureFlag(SystemFeatureFlagVO systemFeatureFlagVO) {
        getDicService().saveDataDicItem("SYSTEM", new SystemDataDicItemEntity(null, null, systemFeatureFlagVO.getFlagName(),
            systemFeatureFlagVO.getFlagValue(), systemFeatureFlagVO.getFlagDesc(), 0, null, null,
            DataDicFlagEnum.ENABLE.getCode()));
    }

    @Override
    public void updateSystemFeatureFlag(SystemFeatureFlagVO systemFeatureFlagVO) {
        var dicModel = getDicModel();
        if (dicModel == null) {
            log.warn("未找到系统数据字典");
            return;
        }
        if (CollUtil.isEmpty(dicModel.getItems())) {
            log.warn("未找到系统数据字典项");
            return;
        }
        var item =
            dicModel.getItems().stream().filter(i -> Objects.equals(i.getLabel(), systemFeatureFlagVO.getFlagName()))
                .findFirst().orElse(null);
        if (item == null) {
            log.warn("未找到系统数据字典项:{}", systemFeatureFlagVO.getFlagName());
            return;
        }
        getDicService().saveDataDicItem(dicModel.getDicCode(),
            new SystemDataDicItemEntity(item.getId(), dicModel.getDicCode(), systemFeatureFlagVO.getFlagName(),
                systemFeatureFlagVO.getFlagValue(), systemFeatureFlagVO.getFlagDesc(), 0, null, null,
                DataDicFlagEnum.ENABLE.getCode()));
    }

    @Override
    public void deleteSystemFeatureFlag(String flagName) {
        getDicService().deleteDataDicItem("SYSTEM", flagName);
    }

    @Override
    public List<SystemFeatureFlagVO> listAllSystemFeatureFlags() {
        var dicModel = getDicModel();
        if (dicModel == null) {
            log.warn("未找到系统数据字典");
            return List.of();
        }
        var items = dicModel.getItems();
        if (CollUtil.isEmpty(items)) {
            log.warn("未找到系统数据字典项");
            return List.of();
        }
        return items.stream().map(item -> {
            var vo = new SystemFeatureFlagVO();
            vo.setFlagName(item.getLabel());
            vo.setFlagValue(item.getValue());
            vo.setFlagDesc(item.getRemark());
            return vo;
        }).collect(Collectors.toList());
    }
}
