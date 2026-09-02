package com.tbs.dic.constant

/**
 * 数据字典模块国际化常量
 * <p>
 * 集中收拢 dic 模块中面向用户的 i18n 资源 key，避免调用方散落硬编码字符串魔法值。
 * key 对应资源文件见 [dic/dic-service/src/main/resources/i18n/messages-dic.properties]（中文）
 * 与 [messages-dic_en_US.properties]（英文），新增 key 须中英文两份同步维护。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
object DicI18nConstant {

    /* ===================== 数据字典（基础） ===================== */

    /**
     * 字典编号为空校验提示：字典编号不能为空
     */
    const val DIC_CODE_EMPTY = "dic.code.empty"

    /**
     * 字典名称为空校验提示：字典名称不能为空
     */
    const val DIC_NAME_EMPTY = "dic.name.empty"

    /**
     * 数据字典不存在校验提示：数据字典[{0}]不存在
     */
    const val DIC_NOT_EXISTS = "dic.not.exists"

    /**
     * 数据字典存在未删除项校验提示：数据字典[{0}]有未删除的项
     */
    const val DIC_HAS_UNDELETED_ITEMS = "dic.has.undeleted.items"

    /* ===================== 数据字典项 ===================== */

    /**
     * 字典项标签为空校验提示：数据字典项[{0}]不能为空
     */
    const val DIC_ITEM_LABEL_EMPTY = "dic.item.label.empty"

    /**
     * 字典项值为空校验提示：数据字典项[{0}]不能为空
     */
    const val DIC_ITEM_VALUE_PARAM_EMPTY = "dic.item.value.param.empty"

    /**
     * 字典项标签已存在校验提示：数据字典项[{0}]已存在
     */
    const val DIC_ITEM_LABEL_EXISTS = "dic.item.label.exists"

    /**
     * 字典项ID不存在校验提示：字典项ID[{0}]不存在
     */
    const val DIC_ITEM_ID_NOT_EXISTS = "dic.item.id.not.exists"

    /**
     * 字典项标签不存在校验提示：数据字典项[{0}]不存在
     */
    const val DIC_ITEM_LABEL_NOT_EXISTS = "dic.item.label.not.exists"
}
