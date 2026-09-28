package com.tbs.app.form;

import cn.hutool.core.util.StrUtil;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Set;

/**
 * 系统通知消息载荷（强约束契约）
 * <p>
 * 系统通知发布的业务消息 JSON 结构强类型表达，与前端接收契约（app-ui-vue push-message.js）逐字段对齐： id 与 type 必填，type/priority
 * 限定白名单取值，timestamp/priority 缺省时自动补齐。 契约校验在发布入口（POST /private/push/send）前置执行，非法载荷直接拒绝，
 * 不再依赖接收端容错丢弃。超出本契约的发布需求由后续开发者自行扩展新端点。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@ToString
@Schema(description = "系统通知消息载荷（强约束契约）")
public class PushNotifyMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * i18n key：消息载荷不能为空
     */
    public static final String I18N_PAYLOAD_REQUIRED = "push.notify.payload.required";

    /**
     * i18n key：消息ID不能为空
     */
    public static final String I18N_ID_REQUIRED = "push.notify.id.required";

    /**
     * i18n key：消息类型不能为空
     */
    public static final String I18N_TYPE_REQUIRED = "push.notify.type.required";

    /**
     * i18n key：消息类型不在白名单
     */
    public static final String I18N_TYPE_INVALID = "push.notify.type.invalid";

    /**
     * i18n key：消息优先级不在白名单
     */
    public static final String I18N_PRIORITY_INVALID = "push.notify.priority.invalid";

    /**
     * 消息类型：通知
     */
    public static final String TYPE_NOTIFY = "notify";

    /**
     * 消息类型：警告
     */
    public static final String TYPE_WARNING = "warning";

    /**
     * 消息类型：提醒
     */
    public static final String TYPE_REMINDER = "reminder";

    /**
     * 优先级：低
     */
    public static final String PRIORITY_LOW = "low";

    /**
     * 优先级：普通
     */
    public static final String PRIORITY_NORMAL = "normal";

    /**
     * 优先级：高
     */
    public static final String PRIORITY_HIGH = "high";

    /**
     * 优先级：紧急
     */
    public static final String PRIORITY_URGENT = "urgent";

    /**
     * 消息类型白名单
     */
    public static final Set<String> SUPPORTED_TYPES = Set.of(TYPE_NOTIFY, TYPE_WARNING, TYPE_REMINDER);

    /**
     * 消息优先级白名单
     */
    public static final Set<String> SUPPORTED_PRIORITIES =
        Set.of(PRIORITY_LOW, PRIORITY_NORMAL, PRIORITY_HIGH, PRIORITY_URGENT);

    /**
     * 消息唯一标识（接收端据此去重，必填）
     */
    @Schema(description = "消息唯一标识", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /**
     * 消息类型（notify/warning/reminder，必填）
     */
    @Schema(description = "消息类型: notify/warning/reminder", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    /**
     * 标题（可选）
     */
    @Schema(description = "标题")
    private String title;

    /**
     * 正文（可选）
     */
    @Schema(description = "正文")
    private String content;

    /**
     * 毫秒时间戳（可选，缺省取发布时刻）
     */
    @Schema(description = "毫秒时间戳，缺省取发布时刻")
    private Long timestamp;

    /**
     * 优先级（low/normal/high/urgent，可选，缺省 normal）
     */
    @Schema(description = "优先级: low/normal/high/urgent，缺省 normal")
    private String priority;

    /**
     * 发送者标识（可选）
     */
    @Schema(description = "发送者标识")
    private String sender;

    /**
     * 点击跳转地址（可选）
     */
    @Schema(description = "点击跳转地址")
    private String url;

    /**
     * 契约校验与规范化
     * <p>
     * 校验必填字段（id、type）与白名单取值（type、priority），失败抛出带 i18n 文案的用户输入错误； 规范化可缺省字段：priority 缺省补 normal，timestamp 缺省补当前毫秒。
     * </p>
     *
     * @return 规范化后的当前对象（链式）
     */
    public PushNotifyMessage prepare() {
        AssertUtil.isTrue(StrUtil.isEmpty(id)).i18n(I18N_ID_REQUIRED).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(type)).i18n(I18N_TYPE_REQUIRED).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        AssertUtil.isTrue(!SUPPORTED_TYPES.contains(type)).i18n(I18N_TYPE_INVALID).params(type)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        if (priority == null) {
            priority = PRIORITY_NORMAL;
        } else {
            AssertUtil.isTrue(!SUPPORTED_PRIORITIES.contains(priority)).i18n(I18N_PRIORITY_INVALID).params(priority)
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        }
        if (timestamp == null) {
            timestamp = System.currentTimeMillis();
        }
        return this;
    }

}
