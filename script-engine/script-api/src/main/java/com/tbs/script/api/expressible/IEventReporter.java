package com.tbs.script.api.expressible;

import com.tbs.common.provider.IRouteMeta;
import com.tbs.common.provider.vo.InstanceMeta;
import com.tbs.script.api.expressible.vo.EventMeta;
import com.tbs.script.api.expressible.vo.param.ParamValue;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 事件上报器接口。
 *
 * <p>一个上报器可以承载多个事件（{@link #getEvents()}），调用 {@link #report(String, Collection)}
 * 时通过 eventCode 指定本次上报的事件。eventCode 仅在当前上报器内唯一， 全局唯一定位一个事件使用 (eventReporterId, eventCode) 组合。</p>
 */
public interface IEventReporter extends IRouteMeta {

    /**
     * 上报事件。
     *
     * @param eventCode   事件编码，必须在 {@link #getEvents()} 中声明
     * @param paramValues 事件参数值，顺序/数量应满足 {@link EventMeta#getParamTypes()}
     */
    void report(String eventCode, Collection<ParamValue> paramValues);

    /**
     * 获取本上报器支持的所有事件元信息。
     *
     * @return 事件元信息列表，不可为 null
     */
    List<EventMeta> getEvents();

    /**
     * 获取事件编码集合，便于调用方做快速判断。
     * <p>默认从 {@link #getEvents()} 派生，实现类通常无需覆写。</p>
     */
    default Set<String> getEventCodes() {
        List<EventMeta> events = getEvents();
        if (events == null || events.isEmpty()) {
            return Set.of();
        }
        return events.stream().filter(Objects::nonNull).map(EventMeta::getCode).filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 按 eventCode 获取事件元信息，未声明时返回 null。
     * <p>默认从 {@link #getEvents()} 派生，实现类通常无需覆写。</p>
     */
    default EventMeta getEventMeta(String eventCode) {
        if (eventCode == null) {
            return null;
        }
        List<EventMeta> events = getEvents();
        if (events == null) {
            return null;
        }
        return events.stream().filter(Objects::nonNull).filter(e -> eventCode.equals(e.getCode())).findFirst()
            .orElse(null);
    }

    /**
     * 获取事件上报器ID，作为路由 business，全局唯一。
     */
    String getEventReporterId();

    /**
     * 获取事件上报器名称（展示用）。
     */
    String getEventReporterName();

    /**
     * 获取事件上报器描述。
     */
    String getEventReporterDescription();

    @Override
    default InstanceMeta getMeta() {
        return IRouteMeta.super.getMeta().setBusiness(getEventReporterId());
    }
}
