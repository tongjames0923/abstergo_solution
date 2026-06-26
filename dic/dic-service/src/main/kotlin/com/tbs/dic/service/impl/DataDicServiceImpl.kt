package com.tbs.dic.service.impl

import com.tbs.common.error.AssertUtil
import com.tbs.common.util.base.StrUtil
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
import javax.annotation.Resource


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
            }.toList();
    }

    override fun getDicCodesByNamesLike(name: String): List<String> {
        return QueryEngine.getEntityQueryWrapper(dicEntity).query().where()
            .leftLike(SystemDataDicEntity::getDataDicName, name).equal(
                SystemDataDicEntity::getFlag, DataDicFlagEnum.ENABLE.code
            ).pour().fetch().stream().map {
                it.dataDicCode
            }.toList();
    }


    @Cacheable(cacheNames = ["data_dic"], key = "#code")
    override fun getDicByCode(
        code: String,
        insertItem: Boolean
    ): List<DicModel> {
        val QW = QueryEngine.getEntityQueryWrapper(dicEntity).query().where()
            .equal(SystemDataDicEntity::getDataDicCode, code).equal(
                SystemDataDicEntity::getFlag, DataDicFlagEnum.ENABLE.code
            ).pour().fetchOne();
        if (QW == null) {
            return emptyList();
        }
        val items = QueryEngine.getEntityQueryWrapper(dicItemEntity).query().where().equal(
            SystemDataDicItemEntity::getDicCode, code
        ).equal(
            SystemDataDicItemEntity::getFlag, DataDicFlagEnum.ENABLE.code
        ).pour().fetch().stream()
            .filter { e -> e != null }
            .sorted { entity, entity1 ->
                val s1 = entity.sort ?: 0;
                val s2 = entity1.sort ?: 0;
                return@sorted s1.compareTo(s2)
            }.map {
                DicItemModel(it.id, it.label, it.value, it.remark, it.flag)
            }.toList();
        return listOf(DicModel(QW.dataDicCode, QW.dataDicName, QW.remark, QW.flag, items))
    }

    @CacheEvict(
        cacheNames = ["data_dic"],
        key = "#dic.dataDicCode",
        condition = "#dic!=null&&#dic.dataDicCode!=null"
    )
    override fun saveDataDic(dic: SystemDataDicEntity): SystemDataDicEntity? {
        val old = QueryEngine.getEntityQueryWrapper(dicEntity).query().where()
            .equal(SystemDataDicEntity::getDataDicCode, dic.dataDicCode)
            .`in`(
                SystemDataDicEntity::getFlag, Arrays.asList(
                    DataDicFlagEnum.ENABLE.code,
                    DataDicFlagEnum.DISABLE.code
                )
            )
            .pour().fetchOne();
        if (dic.id == null) {
            AssertUtil.isTrue(old != null, "数据字典[${dic.dataDicCode}]已存在")
            val dic = SystemDataDicEntity(
                null, dic.dataDicName, dic.dataDicCode, DataDicFlagEnum.ENABLE.code,
                dic.remark, Date(), null,
            )
            dataDicDao.insert(dic)
            return dic;
        } else {
            val e = dataDicDao.selectByPrimaryKey(dic.id);
            AssertUtil.isTrue(e == null, "数据字典ID[${dic.id}]不存在")
            AssertUtil.isTrue(old.id != e.id, "数据字典[${dic.dataDicCode}]已存在")
            if (StrUtil.isNotEmpty(dic.remark)) {
                e.remark = dic.remark;
            }
            if (StrUtil.isNotEmpty(dic.dataDicName)) {
                e.dataDicName = dic.dataDicName;
            }
            e.updateTime = Date();
            dataDicDao.updateByPrimaryKey(dic)
            return dic;
        }

    }

    @CacheEvict(
        cacheNames = ["data_dic"],
        key = "#dicCode"
    )
    override fun saveDataDicItem(dicCode: String, item: SystemDataDicItemEntity) {
        AssertUtil.hasText(item.label, "数据字典项[${item.label}]不能为空")
        AssertUtil.hasText(item.value, "数据字典项[${item.value}]不能为空")

        val old = QueryEngine.getEntityQueryWrapper(dicEntity).query().where()
            .equal(SystemDataDicEntity::getDataDicCode, dicCode)
            .`in`(
                SystemDataDicEntity::getFlag, Arrays.asList(
                    DataDicFlagEnum.ENABLE.code,
                    DataDicFlagEnum.DISABLE.code
                )
            )
            .pour().fetchOne();
        AssertUtil.isTrue(old == null, "数据字典[${dicCode}]不存在")

        val oi = QueryEngine.getEntityQueryWrapper(dicItemEntity).query().where().equal(
            SystemDataDicItemEntity::getLabel, item.label
        ).equal(
            SystemDataDicItemEntity::getDicCode, dicCode
        ).`in`(
            SystemDataDicItemEntity::getFlag, Arrays.asList(
                DataDicFlagEnum.ENABLE.code,
                DataDicFlagEnum.DISABLE.code
            )
        ).pour().fetchOne();
        if (item.id == null) {
            AssertUtil.isTrue(oi != null, "数据字典项[${item.label}]已存在")
            val dic = SystemDataDicItemEntity(
                null, dicCode, item.label, item.value, item.remark, item.sort,
                Date(), null, DataDicFlagEnum.ENABLE.code
            )
            dataDicItemDao.insert(dic);
        } else {
            val e = dataDicItemDao.selectByPrimaryKey(item.id);
            AssertUtil.isTrue(e == null, "数据字典项ID[${item.id}]不存在")
            AssertUtil.isTrue(oi.id != e.id, "数据字典项[${item.label}]已存在")
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
        ).`in`(
            SystemDataDicItemEntity::getFlag, Arrays.asList(
                DataDicFlagEnum.ENABLE.code,
                DataDicFlagEnum.DISABLE.code
            )
        ).pour().fetchOne();
        AssertUtil.isTrue(QW == null, "数据字典项[${label}]不存在")
        QW.flag = flagEnum.code
        QW.updateTime = Date()
        dataDicItemDao.updateByPrimaryKey(QW)
    }


    private fun setDicFlag(dicCode: String, flagEnum: DataDicFlagEnum) {
        val QW = QueryEngine.getEntityQueryWrapper(dicEntity).query().where().equal(
            SystemDataDicEntity::getDataDicCode, dicCode
        ).`in`(
            SystemDataDicEntity::getFlag, Arrays.asList(
                DataDicFlagEnum.ENABLE.code,
                DataDicFlagEnum.DISABLE.code
            )
        ).pour().fetchOne();
        AssertUtil.isTrue(QW == null, "数据字典[${dicCode}]不存在")

        if (Objects.equals(flagEnum.code, DataDicFlagEnum.DELETE.code)) {
            val item = QueryEngine.getEntityQueryWrapper(dicItemEntity)
                .query().where()
                .equal(SystemDataDicItemEntity::getDicCode, dicCode)
                .`in`(
                    SystemDataDicItemEntity::getFlag, Arrays.asList(
                        DataDicFlagEnum.ENABLE.code, DataDicFlagEnum.DISABLE.code
                    )
                ).pour().fetchOne();
            AssertUtil.isTrue(item != null, "数据字典[${dicCode}]有未删除的项")
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
