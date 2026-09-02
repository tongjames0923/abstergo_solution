package com.tbs.app.controller.constant;

/**
 * 应用层国际化常量
 * <p>
 * 集中收拢 app 模块中面向用户的 i18n 资源 key，避免调用方散落硬编码字符串魔法值。
 * key 对应资源文件见 {@code app/src/main/resources/i18n/messages-app.properties}（中文）
 * 与 {@code messages-app_en_US.properties}（英文），新增 key 须中英文两份同步维护。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class AppI18nContant {

    private AppI18nContant() {
    }

    /* ===================== 对象存储（OSS） ===================== */

    /**
     * 上传文件为空校验提示：上传文件不能为空
     */
    public static final String OSS_FILE_EMPTY = "oss.file.empty";

    /**
     * 对象存储键为空校验提示：对象键不能为空
     */
    public static final String OSS_OBJECT_KEY_EMPTY = "oss.object.key.empty";

    /**
     * 上传任务ID为空校验提示：上传任务ID不能为空
     */
    public static final String OSS_TASK_ID_EMPTY = "oss.task.id.empty";
}
