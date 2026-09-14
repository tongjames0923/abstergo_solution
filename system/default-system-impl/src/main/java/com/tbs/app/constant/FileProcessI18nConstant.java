package com.tbs.app.constant;

/**
 * 文件处理国际化常量
 * <p>
 * 集中收拢对象存储（含公开下载码）与数据处理功能面向用户的 i18n 资源 key，
 * 避免调用方散落硬编码字符串魔法值。
 * key 对应资源文件见 {@code default-system-impl/src/main/resources/messages-system.properties}（中文）
 * 与 {@code messages-system_en_US.properties}（英文），新增 key 须中英文两份同步维护
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class FileProcessI18nConstant {

    private FileProcessI18nConstant() {
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

    /**
     * 下载唯一码为空校验提示：下载唯一码不能为空
     */
    public static final String OSS_DOWNLOAD_CODE_EMPTY = "oss.download.code.empty";

    /**
     * 下载唯一码不存在提示：下载唯一码不存在：{0}
     */
    public static final String OSS_DOWNLOAD_CODE_NOT_FOUND = "oss.download.code.not.found";

    /**
     * 下载码无效或已过期提示（通用语义，不泄露存储细节）
     */
    public static final String OSS_DOWNLOAD_CODE_INVALID = "oss.download.code.invalid";

    /**
     * 下载码对应存储对象不存在提示：存储对象不存在：{0}
     */
    public static final String OSS_DOWNLOAD_OBJECT_NOT_EXIST = "oss.download.code.object.not.exist";

    /**
     * 下载唯一码生成失败提示
     */
    public static final String OSS_DOWNLOAD_CODE_GENERATE_FAILED = "oss.download.code.generate.failed";

    /**
     * 自定义下载码格式非法提示：下载码须为4~64位Base64字符（字母、数字、-、_）：{0}
     */
    public static final String OSS_DOWNLOAD_CODE_FORMAT_INVALID = "oss.download.code.format.invalid";

    /**
     * 自定义下载码已存在提示：下载唯一码已存在：{0}
     */
    public static final String OSS_DOWNLOAD_CODE_ALREADY_EXISTS = "oss.download.code.already.exists";

    /* ===================== 数据处理 ===================== */

    /**
     * 数据处理文件为空校验提示：上传文件不能为空
     */
    public static final String DATA_HANDLE_FILE_EMPTY = "data.handle.file.empty";

    /**
     * 数据处理器ID为空校验提示：处理器ID不能为空
     */
    public static final String DATA_HANDLE_HANDLER_ID_EMPTY = "data.handle.handler.id.empty";

    /**
     * 数据处理器未找到提示：未找到数据处理器，ID：{0}
     */
    public static final String DATA_HANDLE_HANDLER_NOT_FOUND = "data.handle.handler.not.found";
}
