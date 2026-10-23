package com.tbs.dic.service.impl;

import com.tbs.common.constant.ProviderConstant;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.dic.constant.DicI18nConstant;
import com.tbs.dic.entity.SystemDataDicEntity;
import com.tbs.dic.entity.SystemDataDicItemEntity;
import com.tbs.dic.enums.DataDicCompareEnum;
import com.tbs.dic.enums.DataDicFlagEnum;
import com.tbs.dic.mapper.DataDicItemMapper;
import com.tbs.dic.mapper.DataDicMapper;
import com.tbs.dic.model.DicItemModel;
import com.tbs.dic.model.DicModel;
import com.tbs.dic.service.IDataDicService;
import com.tbs.sql.service.QueryEngine;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据字典服务 Dubbo 发布实现
 * <p>
 * group 显式声明为默认业务分区（{@code default}），使发布坐标自说明，
 * 与发现侧「未声明 group 兜底 default」的语义保持一致。
 * </p>
 */
@DubboService
public class DataDicServiceImpl implements IDataDicService {

    @Resource
    private DataDicMapper dataDicDao;

    @Resource
    private DataDicItemMapper dataDicItemDao;

    private final SystemDataDicEntity dicEntity = new SystemDataDicEntity();
    private final SystemDataDicItemEntity dicItemEntity = new SystemDataDicItemEntity();

    @Override
    public List<String> getDicCodesByCodeLike(String code) {
        return QueryEngine.getEntityQueryWrapper(dicEntity).query().where()
            .leftLike(SystemDataDicEntity::getDataDicCode, code).equal(
                SystemDataDicEntity::getFlag, DataDicFlagEnum.ENABLE.getCode()
            ).pour().fetch().stream().map(
                SystemDataDicEntity::getDataDicCode
            ).collect(Collectors.toList());
    }

    @Override
    public List<String> getDicCodesByNamesLike(String name) {
        return QueryEngine.getEntityQueryWrapper(dicEntity).query().where()
            .leftLike(SystemDataDicEntity::getDataDicName, name).equal(
                SystemDataDicEntity::getFlag, DataDicFlagEnum.ENABLE.getCode()
            ).pour().fetch().stream().map(
                SystemDataDicEntity::getDataDicCode
            ).collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "data_dic", key = "#code")
    @Override
    public List<DicModel> getDicByCode(String code, boolean insertItem) {
        SystemDataDicEntity qw = QueryEngine.getEntityQueryWrapper(dicEntity).query().where()
            .equal(
                SystemDataDicEntity::getDataDicCode, code
            ).pour().fetchOne();
        if (qw == null) {
            return Collections.emptyList();
        }
        List<DicItemModel> items = QueryEngine.getEntityQueryWrapper(dicItemEntity).query().where().equal(
            SystemDataDicItemEntity::getDicCode, code
        ).pour().fetch().stream()
            .filter(Objects::nonNull)
            .sorted((entity, entity1) -> {
                int s1 = entity.getSort() == null ? 0 : entity.getSort();
                int s2 = entity1.getSort() == null ? 0 : entity1.getSort();
                return Integer.compare(s1, s2);
            }).map(entity -> new DicItemModel(entity.getId(), entity.getLabel(), entity.getValue(), entity.getRemark(), entity.getFlag()))
            .collect(Collectors.toList());
        return Collections.singletonList(new DicModel(qw.getId(), qw.getDataDicCode(), qw.getDataDicName(), qw.getRemark(), qw.getFlag(), items));
    }

    @CacheEvict(
        cacheNames = "data_dic",
        key = "#dic.dataDicCode",
        condition = "#dic!=null&&#dic.dataDicCode!=null"
    )
    @Override
    public SystemDataDicEntity saveDataDic(SystemDataDicEntity dic) {
        AssertUtil.isTrue(StrUtil.isEmpty(dic.getDataDicCode())).i18n(DicI18nConstant.DIC_CODE_EMPTY)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(dic.getDataDicName())).i18n(DicI18nConstant.DIC_NAME_EMPTY)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

        SystemDataDicEntity old = QueryEngine.getEntityQueryWrapper(dicEntity).query().where()
            .equal(SystemDataDicEntity::getDataDicCode, dic.getDataDicCode())
            .pour().fetchOne();
        if (old == null) {
            SystemDataDicEntity newDic = new SystemDataDicEntity(
                null, dic.getDataDicName(), dic.getDataDicCode(), DataDicFlagEnum.ENABLE.getCode(),
                dic.getRemark(), new Date(), null
            );
            dataDicDao.insert(newDic);
            return newDic;
        } else {
            SystemDataDicEntity e = old;
            if (StrUtil.isNotEmpty(dic.getRemark())) {
                e.setRemark(dic.getRemark());
            }
            if (StrUtil.isNotEmpty(dic.getDataDicName())) {
                e.setDataDicName(dic.getDataDicName());
            }
            e.setUpdateTime(new Date());
            dataDicDao.updateByPrimaryKey(e);
            return dic;
        }
    }

    @CacheEvict(
        cacheNames = "data_dic",
        key = "#dicCode"
    )
    @Override
    public void saveDataDicItem(String dicCode, SystemDataDicItemEntity item) {

        AssertUtil.isTrue(StrUtil.isEmpty(dicCode))
            .i18n(DicI18nConstant.DIC_CODE_EMPTY)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();

        AssertUtil.isTrue(StrUtil.isEmpty(item.getLabel())).i18n(DicI18nConstant.DIC_ITEM_LABEL_EMPTY).params(item.getLabel())
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(item.getValue())).i18n(DicI18nConstant.DIC_ITEM_VALUE_PARAM_EMPTY).params(item.getValue())
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

        SystemDataDicEntity old = QueryEngine.getEntityQueryWrapper(dicEntity).query().where()
            .equal(SystemDataDicEntity::getDataDicCode, dicCode)
            .pour().fetchOne();
        AssertUtil.isTrue(old == null).i18n(DicI18nConstant.DIC_NOT_EXISTS).params(dicCode)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

        SystemDataDicItemEntity oi = QueryEngine.getEntityQueryWrapper(dicItemEntity).query().where().equal(
            SystemDataDicItemEntity::getLabel, item.getLabel()
        ).equal(
            SystemDataDicItemEntity::getDicCode, dicCode
        ).pour().fetchOne();
        if (item.getId() == null) {
            AssertUtil.isTrue(oi != null).i18n(DicI18nConstant.DIC_ITEM_LABEL_EXISTS).params(item.getLabel())
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
            SystemDataDicItemEntity newItem = new SystemDataDicItemEntity(
                null, dicCode, item.getLabel(), item.getValue(), item.getRemark(), item.getSort(),
                new Date(), null, DataDicFlagEnum.ENABLE.getCode()
            );
            dataDicItemDao.insert(newItem);
        } else {
            SystemDataDicItemEntity e = dataDicItemDao.selectByPrimaryKey(item.getId());
            AssertUtil.isTrue(e == null).i18n(DicI18nConstant.DIC_ITEM_ID_NOT_EXISTS).params(item.getId())
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
            AssertUtil.isTrue(!Objects.equals(oi.getId(), e.getId())).i18n(DicI18nConstant.DIC_ITEM_LABEL_EXISTS).params(item.getLabel())
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
            if (StrUtil.isNotEmpty(item.getValue())) {
                e.setValue(item.getValue());
            }
            if (StrUtil.isNotEmpty(item.getRemark())) {
                e.setRemark(item.getRemark());
            }
            if (item.getSort() != null) {
                e.setSort(item.getSort());
            }
            e.setUpdateTime(new Date());
            dataDicItemDao.updateByPrimaryKey(e);
        }
    }

    private void setDicItemFlag(String dicCode, String label, DataDicFlagEnum flagEnum) {
        SystemDataDicItemEntity qw = QueryEngine.getEntityQueryWrapper(dicItemEntity).query().where().equal(
            SystemDataDicItemEntity::getDicCode, dicCode
        ).equal(
            SystemDataDicItemEntity::getLabel, label
        ).pour().fetchOne();
        AssertUtil.isTrue(qw == null).i18n(DicI18nConstant.DIC_ITEM_LABEL_NOT_EXISTS).params(label)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        qw.setFlag(flagEnum.getCode());
        qw.setUpdateTime(new Date());
        dataDicItemDao.updateByPrimaryKey(qw);
    }

    private void setDicFlag(String dicCode, DataDicFlagEnum flagEnum) {
        SystemDataDicEntity qw = QueryEngine.getEntityQueryWrapper(dicEntity).query().where().equal(
            SystemDataDicEntity::getDataDicCode, dicCode
        ).pour().fetchOne();
        AssertUtil.isTrue(qw == null).i18n(DicI18nConstant.DIC_NOT_EXISTS).params(dicCode).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();

        if (Objects.equals(flagEnum.getCode(), DataDicFlagEnum.DELETE.getCode())) {
            SystemDataDicItemEntity item = QueryEngine.getEntityQueryWrapper(dicItemEntity)
                .query().where()
                .equal(SystemDataDicItemEntity::getDicCode, dicCode)
                .in(
                    SystemDataDicItemEntity::getFlag, Arrays.asList(
                        DataDicFlagEnum.ENABLE.getCode(), DataDicFlagEnum.DISABLE.getCode()
                    )
                ).pour().fetchOne();
            AssertUtil.isTrue(item != null).i18n(DicI18nConstant.DIC_HAS_UNDELETED_ITEMS).params(dicCode)
                .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        }

        qw.setFlag(flagEnum.getCode());
        qw.setUpdateTime(new Date());
        dataDicDao.updateByPrimaryKey(qw);
    }

    @Override
    public int valueEquals(String dicCode, String label, String value) {

        SystemDataDicItemEntity i = QueryEngine.getEntityQueryWrapper(dicItemEntity)
            .query().where()
            .equal(SystemDataDicItemEntity::getDicCode, dicCode)
            .equal(SystemDataDicItemEntity::getLabel, label).in(
                SystemDataDicItemEntity::getFlag, Arrays.asList(
                    DataDicFlagEnum.ENABLE.getCode()
                )
            ).pour().fetchOne();
        if (i == null) {
            return DataDicCompareEnum.NO_ITEM.getCode();
        }
        SystemDataDicEntity dic = QueryEngine.getEntityQueryWrapper(dicEntity)
            .query().where()
            .equal(SystemDataDicEntity::getDataDicCode, dicCode)
            .in(
                SystemDataDicEntity::getFlag, Arrays.asList(
                    DataDicFlagEnum.ENABLE.getCode()
                )
            ).pour().fetchOne();
        if (dic == null) {
            return DataDicCompareEnum.NO_DIC.getCode();
        }
        return Objects.equals(i.getValue(), value) ? DataDicCompareEnum.EQUALS.getCode() : DataDicCompareEnum.NOT_EQUALS.getCode();
    }

    @CacheEvict(
        cacheNames = "data_dic",
        key = "#dicCode"
    )
    @Override
    public void deleteDataDic(String dicCode) {
        setDicFlag(dicCode, DataDicFlagEnum.DELETE);
    }

    @CacheEvict(
        cacheNames = "data_dic",
        key = "#dicCode"
    )
    @Override
    public void deleteDataDicItem(String dicCode, String label) {
        setDicItemFlag(dicCode, label, DataDicFlagEnum.DELETE);
    }

    @CacheEvict(
        cacheNames = "data_dic",
        key = "#dicCode"
    )
    @Override
    public void enableDataDic(String dicCode) {
        setDicFlag(dicCode, DataDicFlagEnum.ENABLE);
    }

    @CacheEvict(
        cacheNames = "data_dic",
        key = "#dicCode"
    )
    @Override
    public void disableDataDic(String dicCode) {
        setDicFlag(dicCode, DataDicFlagEnum.DISABLE);
    }

    @CacheEvict(
        cacheNames = "data_dic",
        key = "#dicCode"
    )
    @Override
    public void enableDataDicItem(String dicCode, String label) {
        setDicItemFlag(dicCode, label, DataDicFlagEnum.ENABLE);
    }

    @CacheEvict(
        cacheNames = "data_dic",
        key = "#dicCode"
    )
    @Override
    public void disableDataDicItem(String dicCode, String label) {
        setDicItemFlag(dicCode, label, DataDicFlagEnum.DISABLE);
    }
}
