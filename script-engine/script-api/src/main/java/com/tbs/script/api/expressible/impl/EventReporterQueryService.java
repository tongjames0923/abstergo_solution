package com.tbs.script.api.expressible.impl;

import com.tbs.common.provider.IRegistryCenter;
import com.tbs.script.api.expressible.IEventReporter;
import com.tbs.script.api.expressible.vo.EventMeta;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 事件元信息查询服务，供前端配置页使用。
 */
@DubboService
@Service
public class EventReporterQueryService {

    @Resource
    IRegistryCenter registryCenter;

    /** 上报器列表（前端第一步：选哪个上报器/哪个业务域） */
    public List<ReporterSummary> listReporters() {
        return reporters().stream().map(
                r -> new ReporterSummary(r.getEventReporterId(), r.getEventReporterName(), r.getEventReporterDescription(),
                    r.getEvents().stream().map(EventMeta::getCode).collect(Collectors.toList())))
            .collect(Collectors.toList());
    }

    /** 某上报器下的事件 + 参数定义（前端第二步：选事件、渲染表单） */
    public List<EventMeta> listEvents(String reporterId) {
        return reporters().stream().filter(r -> reporterId.equals(r.getEventReporterId())).findFirst()
            .map(IEventReporter::getEvents).orElse(List.of());
    }

    private List<IEventReporter> reporters() {
        return registryCenter.getAllContributors().stream()
            .flatMap(c -> c.getAllInstanceByTypeAndBusinessAndVersion(IEventReporter.class, null, null).stream())
            .map(i -> (IEventReporter)i.getInstance()).collect(Collectors.toList());
    }

    /**
     * 上报器摘要，供前端选择上报器。
     *
     * @param id         上报器ID
     * @param name       上报器名称
     * @param description 上报器描述
     * @param eventCodes 事件编码列表
     */
    public record ReporterSummary(String id, String name, String description, List<String> eventCodes)
        implements Serializable {

        private static final long serialVersionUID = 1L;
    }
}
