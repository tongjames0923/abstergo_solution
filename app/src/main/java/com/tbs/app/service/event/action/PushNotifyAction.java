package com.tbs.app.service.event.action;

import com.tbs.app.form.PushNotifySendForm;
import com.tbs.app.service.push.IPushNotifyService;
import com.tbs.app.vo.PushSendResultVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.script.api.expressible.vo.EnumValue;
import com.tbs.script.api.expressible.vo.param.ParamType;
import com.tbs.script.api.expressible.vo.param.ParamUiMeta;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 站内通知推送动作。
 *
 * <p>把事件命中后的业务动作接回平台推送能力：复用 {@link IPushNotifyService#send}，
 * 由推送中心完成接收人解析、落库与信令发布。动作参数与 getter 模式一致，
 * 通过 {@link #requestParamTypes()} 暴露给前端动态渲染表单。</p>
 *
 * @author abstergo
 */
@Component
@Slf4j
public class PushNotifyAction extends AbstractActionSupport {

    /**
     * 动作ID
     */
    public static final String ACTION_ID = "PushNotifyAction";

    /**
     * 动作来源标识，写入推送操作日志的 op_source
     */
    private static final String SOURCE_EVENT = "event";

    /**
     * 动作执行顺序
     */
    private static final int ORDER = 100;

    /**
     * 推送通知服务
     */
    @Resource
    private IPushNotifyService pushNotifyService;

    /**
     * 获取动作名称。
     *
     * @return 国际化后的动作名称
     */
    @Override
    public String getActionName() {
        return text("event.action.push.name");
    }

    /**
     * 获取动作ID。
     *
     * @return 动作ID
     */
    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    /**
     * 获取动作描述。
     *
     * @return 国际化后的动作描述
     */
    @Override
    public String getActionDesc() {
        return text("event.action.push.description");
    }

    /**
     * 获取动作执行顺序。
     *
     * @return 执行顺序
     */
    @Override
    public int getOrder() {
        return ORDER;
    }

    /**
     * 声明动作参数：消息类型、标题、正文、优先级、主题与跳转地址。
     *
     * @return 参数定义列表
     */
    @Override
    public List<ParamType> requestParamTypes() {
        List<EnumValue> priorities = List.of(priority(PushNotifySendForm.PRIORITY_LOW, "event.action.push.priority.low"),
            priority(PushNotifySendForm.PRIORITY_NORMAL, "event.action.push.priority.normal"),
            priority(PushNotifySendForm.PRIORITY_HIGH, "event.action.push.priority.high"),
            priority(PushNotifySendForm.PRIORITY_URGENT, "event.action.push.priority.urgent"));
        return List.of(
            ParamType.builder().type("string").name("msgType").label(text("event.action.push.param.msg.type.name"))
                .description(text("event.action.push.param.msg.type.description")).required(true).sort(1)
                .ui(ParamUiMeta.builder().inputType("text").maxLength(64).placeholder("event.notify").build()).build(),
            ParamType.builder().type("string").name("title").label(text("event.action.push.param.title.name"))
                .description(text("event.action.push.param.title.description")).sort(2)
                .ui(ParamUiMeta.builder().inputType("text").maxLength(255).build()).build(),
            ParamType.builder().type("string").name("content").label(text("event.action.push.param.content.name"))
                .description(text("event.action.push.param.content.description")).sort(3)
                .ui(ParamUiMeta.builder().inputType("textarea").maxLength(2000).build()).build(),
            ParamType.builder().type("string").name("priority").label(text("event.action.push.param.priority.name"))
                .description(text("event.action.push.param.priority.description")).isEnum(true).enumValues(priorities)
                .sort(4).ui(ParamUiMeta.builder().inputType("select").defaultValue(PushNotifySendForm.PRIORITY_NORMAL)
                    .build()).build(),
            ParamType.builder().type("string").name("topic").label(text("event.action.push.param.topic.name"))
                .description(text("event.action.push.param.topic.description")).sort(5)
                .ui(ParamUiMeta.builder().inputType("text").maxLength(128).build()).build(),
            ParamType.builder().type("string").name("url").label(text("event.action.push.param.url.name"))
                .description(text("event.action.push.param.url.description")).sort(6)
                .ui(ParamUiMeta.builder().inputType("text").maxLength(512).build()).build());
    }

    /**
     * 执行推送动作。
     *
     * @param paramValues 动作参数值，按声明顺序传入
     */
    @Override
    public void execute(Collection<ParamValue> paramValues) {
        if (CollUtil.isEmpty(paramValues)) {
            log.warn("推送动作未配置参数, actionId={}", ACTION_ID);
            return;
        }
        String msgType = paramString(paramValues, 0);
        AssertUtil.isTrue(StrUtil.isBlank(msgType)).i18n("event.action.push.msg.type.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

        PushNotifySendForm form = new PushNotifySendForm();
        form.setMsgType(msgType.trim());
        form.setTitle(trimToNull(paramString(paramValues, 1)));
        form.setContent(trimToNull(paramString(paramValues, 2)));
        form.setPriority(trimToNull(paramString(paramValues, 3)));
        form.setTopic(trimToNull(paramString(paramValues, 4)));
        form.setUrl(trimToNull(paramString(paramValues, 5)));
        form.setSource(SOURCE_EVENT);

        PushSendResultVO result = pushNotifyService.send(form);
        log.info("事件动作推送完成, actionId={}, msgType={}, messageId={}, receiverCount={}, signalCount={}",
            ACTION_ID, form.getMsgType(), result == null ? null : result.getMessageId(),
            result == null ? null : result.getReceiverCount(), result == null ? null : result.getSignalCount());
    }

    /**
     * 构造优先级枚举项。
     *
     * @param value    枚举值
     * @param labelKey 文案 key
     * @return 枚举项
     */
    private EnumValue priority(String value, String labelKey) {
        return EnumValue.builder().value(value).label(text(labelKey)).build();
    }

    /**
     * 去除首尾空白，空串归一为 null。
     *
     * @param value 原始值
     * @return 处理后的值
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
