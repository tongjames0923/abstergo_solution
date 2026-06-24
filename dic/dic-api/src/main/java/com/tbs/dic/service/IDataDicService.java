package com.tbs.dic.service;

import com.tbs.dic.entity.SystemDataDicEntity;
import com.tbs.dic.entity.SystemDataDicItemEntity;
import com.tbs.dic.enums.DataDicCompareEnum;
import com.tbs.dic.enums.DataDicFlagEnum;
import com.tbs.dic.model.DicModel;

import java.util.List;

public interface IDataDicService {

    /**
     * 根据字典编码模糊查询字典项
     *
     * @param code
     * @return
     */
    List<String> getDicCodesByCodeLike(String code);

    /**
     * 根据字典名称模糊查询字典项
     *
     * @param name
     * @return
     */
    List<String> getDicCodesByNamesLike(String name);

    /**
     * 根据字典编码获取字典项
     *
     * @param code
     * @param insertItem 是否加入字典项数据
     * @return
     */
    List<DicModel> getDicByCode(String code, boolean insertItem);

    /**
     * 保存字典项
     *
     * @param dic
     * @return
     */
    SystemDataDicEntity saveDataDic(SystemDataDicEntity dic);

    /**
     * 插入字典项
     *
     * @param dicCode
     * @param item
     */
    void saveDataDicItem(String dicCode, SystemDataDicItemEntity item);

    /**
     * 设置字典项的标志
     *
     * @param dicCode  字典编码
     * @param id       字典项id
     * @param flagEnum 标志
     */
    void setDicItemFlag(String dicCode, String label, DataDicFlagEnum flagEnum);

    /**
     * 设置字典的标志
     *
     * @param dicCode  字典编码
     * @param flagEnum 标志
     */
    void setDicFlag(String dicCode, DataDicFlagEnum flagEnum);

    /**
     * 匹配字典项值是否一致
     *
     * @param dicCode
     * @param label
     * @param value
     * @return 匹配结果值请见枚举
     * @see DataDicCompareEnum
     *
     */
    int valueEquals(String dicCode, String label, String value);
}
