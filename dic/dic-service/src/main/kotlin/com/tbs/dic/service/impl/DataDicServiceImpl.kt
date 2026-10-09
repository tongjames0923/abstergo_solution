package com.tbs.dic.service.impl

import com.tbs.common.error.AssertUtil
import com.tbs.common.error.ErrorLevelEnum
import com.tbs.common.util.base.StrUtil
import com.tbs.dic.constant.DicI18nConstant
import com.tbs.dic.entity.SystemDataDicEntity
import com.tbs.dic.entity.SystemDataDicItemEntity
import com.tbs.dic.enums.DataDicCompareEnum
import com.tbs.dic.enums.DataDicFlagEnum
import com.tbs.dic.mapper.DataDicItemMapper
import com.tbs.dic.mapper.DataDicMapper
import com.tbs.dic.model.DicItemModel
import com.tbs.dic.model.DicModel
import com.tbs.dic.service.IDataDicService
import com.tbs.sql.service.QueryEngine
import org.apache.dubbo.config.annotation.DubboService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import java.util.*
import java.util.stream.Collectors
import jakarta.annotation.Resource


@DubboService
open class DataDicServiceImpl : IDataDicService {

    @Resource
    private lateinit var dataDicDao: DataDicMapper;

    @Resource
    private lateinit var dataDicItemDao: DataDicItemMapper;

    private val dicEntity = SystemDataDicEntity();
    private val dicItemEntity = SystemDataDicItemEntity();

    override fun getDicCodesByCodeLike(code: String): List<String> {
        return QueryEngine.getEntityQueryWrapper(dicEntity).query().where()
            .leftLike(SystemDataDicEntity::getDataDicCode, code).equal(
                SystemDataDicEntity::getFlag, DataDicFlagEnum.ENABLE.code
            ).pour().fetch().stream().map {
                it.dataDicCode
            }.collect(Collectors.toList())
    }

    override fun getDicCodesByNamesLike(name: String): List<String> {
        return QueryEngine.getEntityQueryWrapper(dicEntity).query().where()
            .leftLike(SystemDataDicEntity::getDataDicName, name).equal(
                SystemDataDicEntity::getFlag, DataDicFlagEnum.ENABLE.code
            ).pour().fetch().stream().map {
                it.dataDicCode
            }.collect(Collectors.toList());
    }


    @Cacheable(cacheNames = ["data_dic"], key = "#code")
    override fun getDicByCode(
        code: String,
        insertItem: Boolean
    ): List<DicModel> {
        val QW = QueryEngine.getEntityQueryWrapper(dicEntity).query().where()
            .equal(
                SystemDataDicEntity::getDataDicCode, code
            ).pour().fetchOne();
        if (QW == null) {
            return emptyList();
        }
        val items = QueryEngine.getEntityQueryWrapper(dicItemEntity).query().where().equal(
            SystemDataDicItemEntity::getDicCode, code
        ).pour().fetch().stream()
            .filter { e -> e != null }
            .sorted { entity, entity1 ->
                val s1 = entity.sort ?: 0;
                val s2 = entity1.sort ?: 0;
                return@sorted s1.compareTo(s2)
            }.map {
                DicItemModel(it.id, it.label, it.value, it.remark, it.flag)
            }.collect(Collectors.toList());
        return listOf(DicModel(QW.id, QW.dataDicCode, QW.dataDicName, QW.remark, QW.flag, items))
    }

    @CacheEvict(
        cacheNames = ["data_dic"],
        key = "#dic.dataDicCode",
        condition = "#dic!=null&&#dic.dataDicCode!=null"
    )
    override fun saveDataDic(dic: SystemDataDicEntity): SystemDataDicEntity? {
        AssertUtil.isTrue(StrUtil.isEmpty(dic.dataDicCode)).i18n(DicI18nConstant.DIC_CODE_EMPTY)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt()
        AssertUtil.isTrue(StrUtil.isEmpty(dic.dataDicName)).i18n(DicI18nConstant.DIC_NAME_EMPTY)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt()

        val old = QueryEngine.getEntityQueryWrapper(dicEntity).query().where()
            .equal(SystemDataDicEntity::getDataDicCode, dic.dataDicCode)
            .pour().fetchOne();
        if (old == null) {
            val dic = SystemDataDicEntity(
                null, dic.dataDicName, dic.dataDicCode, DataDicFlagEnum.ENABLE.code,
                dic.remark, Date(), null,
            )
            dataDicDao.insert(dic)
            return dic;
        } else {
            var e = old;
            if (StrUtil.isNotEmpty(dic.remark)) {
                e.remark = dic.remark;
            }
            if (StrUtil.isNotEmpty(dic.dataDicName)) {
                e.dataDicName = dic.dataDicName;
            }
            e.updateTime = Date();
            dataDicDao.updateByPrimaryKey(e)
            return dic;
        }

    }

    @CacheEvict(
        cacheNames = ["data_dic"],
        key = "#dicCode"
    )
    override fun saveDataDicItem(dicCode: String, item: SystemDataDicItemEntity) {

        AssertUtil.isTrue(StrUtil.isEmpty(dicCode))
            .i18n(DicI18nConstant.DIC_CODE_EMPTY)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt()

        AssertUtil.isTrue(StrUtil.isEmpty(item.label)).i18n(DicI18nConstant.DIC_ITEM_LABEL_EMPTY).params(item.label)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt()
        AssertUtil.isTrue(StrUtil.isEmpty(item.value)).i18n(DicI18nConstant.DIC_ITEM_VALUE_PARAM_EMPTY).params(item.value)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt()

        val old = QueryEngine.getEntityQueryWrapper(dicEntity).query().where()
            .equal(SystemDataDicEntity::getDataDicCode, dicCode)
            .pour().fetchOne();
        AssertUtil.isTrue(old == null).i18n(DicI18nConstant.DIC_NOT_EXISTS).params(dicCode)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt()

        val oi = QueryEngine.getEntityQueryWrapper(dicItemEntity).query().where().equal(
            SystemDataDicItemEntity::getLabel, item.label
        ).equal(
            SystemDataDicItemEntity::getDicCode, dicCode
        ).pour().fetchOne();
        if (item.id == null) {
            AssertUtil.isTrue(oi != null).i18n(DicI18nConstant.DIC_ITEM_LABEL_EXISTS).params(item.label)
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt()
            val dic = SystemDataDicItemEntity(
                null, dicCode, item.label, item.value, item.remark, item.sort,
                Date(), null, DataDicFlagEnum.ENABLE.code
            )
            dataDicItemDao.insert(dic);
        } else {
            val e = dataDicItemDao.selectByPrimaryKey(item.id);
            AssertUtil.isTrue(e == null).i18n(DicI18nConstant.DIC_ITEM_ID_NOT_EXISTS).params(item.id)
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt()
            AssertUtil.isTrue(oi.id != e.id).i18n(DicI18nConstant.DIC_ITEM_LABEL_EXISTS).params(item.label)
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt()
            if (StrUtil.isNotEmpty(item.value)) {
                e.value = item.value
            }
            if (StrUtil.isNotEmpty(item.remark)) {
                e.remark = item.remark
            }
            if (item.sort != null) {
                e.sort = item.sort
            }
            e.updateTime = Date()
            dataDicItemDao.updateByPrimaryKey(e)
        }
    }


    private fun setDicItemFlag(
        dicCode: String,
        label: String,
        flagEnum: DataDicFlagEnum
    ) {
        val QW = QueryEngine.getEntityQueryWrapper(dicItemEntity).query().where().equal(
            SystemDataDicItemEntity::getDicCode, dicCode
        ).equal(
            SystemDataDicItemEntity::getLabel, label
        ).pour().fetchOne();
        AssertUtil.isTrue(QW == null).i18n(DicI18nConstant.DIC_ITEM_LABEL_NOT_EXISTS).params(label)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt()
        QW.flag = flagEnum.code
        QW.updateTime = Date()
        dataDicItemDao.updateByPrimaryKey(QW)
    }


    private fun setDicFlag(dicCode: String, flagEnum: DataDicFlagEnum) {
        val QW = QueryEngine.getEntityQueryWrapper(dicEntity).query().where().equal(
            SystemDataDicEntity::getDataDicCode, dicCode
        ).pour().fetchOne();
        AssertUtil.isTrue(QW == null).i18n(DicI18nConstant.DIC_NOT_EXISTS).params(dicCode).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt()

        if (Objects.equals(flagEnum.code, DataDicFlagEnum.DELETE.code)) {
            val item = QueryEngine.getEntityQueryWrapper(dicItemEntity)
                .query().where()
                .equal(SystemDataDicItemEntity::getDicCode, dicCode)
                .`in`(
                    SystemDataDicItemEntity::getFlag, Arrays.asList(
                        DataDicFlagEnum.ENABLE.code, DataDicFlagEnum.DISABLE.code
                    )
                ).pour().fetchOne();
            AssertUtil.isTrue(item != null).i18n(DicI18nConstant.DIC_HAS_UNDELETED_ITEMS).params(dicCode)
                .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt()
        }

        QW.flag = flagEnum.code
        QW.updateTime = Date()
        dataDicDao.updateByPrimaryKey(QW)
    }

    override fun valueEquals(dicCode: String, label: String, value: String): Int {

        var i = QueryEngine.getEntityQueryWrapper(dicItemEntity)
            .query().where()
            .equal(SystemDataDicItemEntity::getDicCode, dicCode)
            .equal(SystemDataDicItemEntity::getLabel, label).`in`(
                SystemDataDicItemEntity::getFlag, Arrays.asList(
                    DataDicFlagEnum.ENABLE.code
                )
            ).pour().fetchOne();
        if (i == null) {
            return DataDicCompareEnum.NO_ITEM.code
        }
        val dic = QueryEngine.getEntityQueryWrapper(dicEntity)
            .query().where()
            .equal(SystemDataDicEntity::getDataDicCode, dicCode)
            .`in`(
                SystemDataDicEntity::getFlag, Arrays.asList(
                    DataDicFlagEnum.ENABLE.code
                )
            ).pour().fetchOne();
        if (dic == null) {
            return DataDicCompareEnum.NO_DIC.code
        }
        return if (i.value == value) DataDicCompareEnum.EQUALS.code else DataDicCompareEnum.NOT_EQUALS.code

    }

    @CacheEvict(
        cacheNames = ["data_dic"],
        key = "#dicCode",
    )
    override fun deleteDataDic(dicCode: String) {
        setDicFlag(dicCode, DataDicFlagEnum.DELETE)
    }

    @CacheEvict(
        cacheNames = ["data_dic"],
        key = "#dicCode",
    )
    override fun deleteDataDicItem(dicCode: String, label: String) {
        setDicItemFlag(dicCode, label, DataDicFlagEnum.DELETE)
    }

    @CacheEvict(
        cacheNames = ["data_dic"],
        key = "#dicCode",
    )
    override fun enableDataDic(dicCode: String) {
        setDicFlag(dicCode, DataDicFlagEnum.ENABLE)
    }

    @CacheEvict(
        cacheNames = ["data_dic"],
        key = "#dicCode",
    )
    override fun disableDataDic(dicCode: String) {
        setDicFlag(dicCode, DataDicFlagEnum.DISABLE)
    }

    @CacheEvict(
        cacheNames = ["data_dic"],
        key = "#dicCode",
    )
    override fun enableDataDicItem(dicCode: String, label: String) {
        setDicItemFlag(dicCode, label, DataDicFlagEnum.ENABLE)
    }

    @CacheEvict(
        cacheNames = ["data_dic"],
        key = "#dicCode",
    )
    override fun disableDataDicItem(dicCode: String, label: String) {
        setDicItemFlag(dicCode, label, DataDicFlagEnum.DISABLE)
    }
}
