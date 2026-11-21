package com.tbs.app.service.event.reporter;

import com.tbs.common.i18n.I18nUtil;
import com.tbs.script.api.expressible.IEventReporter;
import com.tbs.script.api.expressible.vo.EventMeta;
import com.tbs.script.api.expressible.vo.param.ParamType;
import com.tbs.script.api.expressible.vo.param.ParamUiMeta;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 业务事件上报器。
 *
 * <p>声明用户新增、修改、删除、登出四类业务事件及其参数定义，供前端订阅编排页选择事件、
 * 渲染模拟上报表单。业务代码通过 {@code EventReportSupport} 调用编排层完成上报，
 * 本实现只承载事件声明，并对上报动作做审计日志记录。</p>
 *
 * @author abstergo
 */
@Component
@Slf4j
public class BusinessEventReporter implements IEventReporter {

    /**
     * 上报器ID
     */
    public static final String REPORTER_ID = "BusinessEventReporter";

    /**
     * 用户创建事件
     */
    public static final String EVENT_USER_CREATED = "user.created";

    /**
     * 用户修改事件
     */
    public static final String EVENT_USER_UPDATED = "user.updated";

    /**
     * 用户删除事件
     */
    public static final String EVENT_USER_DELETED = "user.deleted";

    /**
     * 用户登出事件
     */
    public static final String EVENT_USER_LOGGED_OUT = "user.loggedOut";

    /**
     * 参数名：用户ID
     */
    public static final String PARAM_USER_ID = "userId";

    /**
     * 参数名：用户名
     */
    public static final String PARAM_USER_NAME = "userName";

    /**
     * 上报事件。
     *
     * <p>编排层已完成事件与参数校验，本方法只记录审计日志；对接外部事件总线时可在此扩展转发。</p>
     *
     * @param eventCode   事件编码
     * @param paramValues 事件参数值
     */
    @Override
    public void report(String eventCode, Collection<ParamValue> paramValues) {
        log.info("业务事件上报, reporterId={}, eventCode={}, 参数数量={}", REPORTER_ID, eventCode,
            paramValues == null ? 0 : paramValues.size());
    }

    /**
     * 获取本上报器支持的全部事件元信息。
     *
     * @return 事件元信息列表
     */
    @Override
    public List<EventMeta> getEvents() {
        return List.of(event(EVENT_USER_CREATED, "event.reporter.business.event.user.created.name"),
            event(EVENT_USER_UPDATED, "event.reporter.business.event.user.updated.name"),
            event(EVENT_USER_DELETED, "event.reporter.business.event.user.deleted.name"),
            event(EVENT_USER_LOGGED_OUT, "event.reporter.business.event.user.logged.out.name"));
    }

    /**
     * 获取事件上报器ID。
     *
     * @return 上报器ID
     */
    @Override
    public String getEventReporterId() {
        return REPORTER_ID;
    }

    /**
     * 获取事件上报器名称。
     *
     * @return 国际化后的名称
     */
    @Override
    public String getEventReporterName() {
        return text("event.reporter.business.name");
    }

    /**
     * 获取事件上报器描述。
     *
     * @return 国际化后的描述
     */
    @Override
    public String getEventReporterDescription() {
        return text("event.reporter.business.description");
    }

    /**
     * 构造事件元信息。
     *
     * @param code    事件编码
     * @param nameKey 事件名称文案 key
     * @return 事件元信息
     */
    private EventMeta event(String code, String nameKey) {
        String name = text(nameKey);
        return EventMeta.builder().code(code).name(name).label(name).description(name).paramTypes(paramTypes())
            .build();
    }

    /**
     * 声明事件参数：用户ID与用户名。
     *
     * @return 参数定义列表
     */
    private List<ParamType> paramTypes() {
        return List.of(ParamType.builder().type("number").name(PARAM_USER_ID)
                .label(text("event.reporter.business.param.user.id.name"))
                .description(text("event.reporter.business.param.user.id.description")).required(true).sort(1)
                .ui(ParamUiMeta.builder().inputType("number").placeholder("10001").build()).build(),
            ParamType.builder().type("string").name(PARAM_USER_NAME)
                .label(text("event.reporter.business.param.user.name.name"))
                .description(text("event.reporter.business.param.user.name.description")).sort(2)
                .ui(ParamUiMeta.builder().inputType("text").placeholder("admin").maxLength(72).build()).build());
    }

    /**
     * 解析国际化文案。
     *
     * @param key 资源 key
     * @return 文案内容；无可用资源时回退为 key 本身
     */
    private String text(String key) {
        String value = I18nUtil.get(key);
        return value == null || value.isBlank() ? key : value;
    }
}
