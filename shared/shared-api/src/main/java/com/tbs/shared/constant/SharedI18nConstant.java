package com.tbs.shared.constant;

/**
 * 共享模块国际化常量
 * <p>
 * 集中收拢 shared 模块中面向用户的 i18n 资源 key，避免调用方散落硬编码字符串魔法值。 key 对应资源文件见
 * {@code shared/shared-api/src/main/resources/messages-shared.properties}（中文） 与
 * {@code messages-shared_en_US.properties}（英文），新增 key 须中英文两份同步维护。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class SharedI18nConstant {

    private SharedI18nConstant() {
    }

    /* ===================== CRUD 通用校验 ===================== */

    /**
     * 删除时主键为空校验提示：空Id无法被删除
     */
    public static final String CRUD_DELETE_ID_EMPTY = "crud.delete.id.empty";

    /**
     * 保存时实体为空校验提示：空对象无法被保存
     */
    public static final String CRUD_SAVE_ENTITY_EMPTY = "crud.save.entity.empty";

    /**
     * 保存时实体不存在提示：实体数据不存在，无法保存
     */
    public static final String CRUD_SAVE_ENTITY_NOT_EXISTS = "crud.save.entity.not.exists";

    /**
     * 更新时实体不存在校验提示：实体数据不存在，无法更新
     */
    public static final String CRUD_UPDATE_ENTITY_NOT_EXISTS = "crud.update.entity.not.exists";

    /**
     * 删除时实体不存在校验提示：需要删除的实体Id{0} 不存在
     */
    public static final String CRUD_DELETE_ENTITY_NOT_EXISTS = "crud.delete.entity.not.exists";

    /**
     * 审计时实体不存在提示：实体数据不存在，无法处理审计
     */
    public static final String CRUD_AUDIT_ENTITY_NOT_EXISTS = "crud.audit.entity.not.exists";
}
