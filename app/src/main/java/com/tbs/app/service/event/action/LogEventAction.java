package com.tbs.app.service.event.action;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.script.api.expressible.vo.EnumValue;
import com.tbs.script.api.expressible.vo.param.ParamType;
import com.tbs.script.api.expressible.vo.param.ParamUiMeta;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 事件日志动作。
 *
 * <p>最小示例动作：把事件命中信息按指定级别写入应用日志，用于验证「事件 → 条件 → 动作」链路
 * 与调试订阅配置，不产生业务副作用。</p>
 *
 * @author abstergo
 */
@Component
@Slf4j
public class LogEventAction extends AbstractActionSupport {

    /**
     * 动作ID
     */
    public static final String ACTION_ID = "LogEventAction";

    /**
     * 日志级别：信息
     */
    private static final String LEVEL_INFO = "info";

    /**
     * 日志级别：告警
     */
    private static final String LEVEL_WARN = "warn";

    /**
     * 日志级别：错误
     */
    private static final String LEVEL_ERROR = "error";

    /**
     * 动作执行顺序
     */
    private static final int ORDER = 200;

    /**
     * 获取动作名称。
     *
     * @return 国际化后的动作名称
     */
    @Override
    public String getActionName() {
        return text("event.action.log.name");
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
        return text("event.action.log.description");
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
     * 声明动作参数：日志内容与日志级别。
     *
     * @return 参数定义列表
     */
    @Override
    public List<ParamType> requestParamTypes() {
        List<EnumValue> levels = List.of(EnumValue.builder().value(LEVEL_INFO)
                .label(text("event.action.log.level.info")).build(),
            EnumValue.builder().value(LEVEL_WARN).label(text("event.action.log.level.warn")).build(),
            EnumValue.builder().value(LEVEL_ERROR).label(text("event.action.log.level.error")).build());
        return List.of(
            ParamType.builder().type("string").name("message").label(text("event.action.log.param.message.name"))
                .description(text("event.action.log.param.message.description")).required(true).sort(1)
                .ui(ParamUiMeta.builder().inputType("textarea").maxLength(1000)
                    .placeholder(text("event.action.log.param.message.placeholder")).build()).build(),
            ParamType.builder().type("string").name("level").label(text("event.action.log.param.level.name"))
                .description(text("event.action.log.param.level.description")).isEnum(true).enumValues(levels).sort(2)
                .ui(ParamUiMeta.builder().inputType("select").defaultValue(LEVEL_INFO).build()).build());
    }

    /**
     * 执行日志动作。
     *
     * @param paramValues 动作参数值，按声明顺序传入
     */
    @Override
    public void execute(Collection<ParamValue> paramValues) {
        if (CollUtil.isEmpty(paramValues)) {
            log.warn("日志动作未配置参数, actionId={}", ACTION_ID);
            return;
        }
        String message = paramString(paramValues, 0);
        AssertUtil.isTrue(StrUtil.isBlank(message)).i18n("event.action.log.message.required")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        String level = paramString(paramValues, 1);
        String normalized = level == null ? LEVEL_INFO : level.trim().toLowerCase();
        switch (normalized) {
            case LEVEL_WARN -> log.warn("[事件动作] {}", message);
            case LEVEL_ERROR -> log.error("[事件动作] {}", message);
            default -> log.info("[事件动作] {}", message);
        }
    }
}
