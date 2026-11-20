package com.tbs.app.service.event;

import com.tbs.script.api.expressible.orchestration.IEventOrchestrator;
import com.tbs.script.api.expressible.orchestration.vo.EventEnvelope;
import com.tbs.script.api.expressible.orchestration.vo.EventReportResult;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 事件上报门面。
 *
 * <p>业务代码埋点的统一入口：把「上报器 + 事件编码 + 业务参数」组装为 {@link EventEnvelope}
 * 后交给 {@link IEventOrchestrator}，由编排层完成上报与订阅分发。</p>
 *
 * <p>埋点不得影响主业务：{@link #reportQuietly} 捕获全部异常并降级为失败回执，
 * 仅记录告警日志；需要感知上报结果的调试场景可调用 {@link #report}。</p>
 *
 * @author abstergo
 */
@Service
@Slf4j
public class EventReportSupport {

    /**
     * 事件编排门面
     */
    @Resource
    private IEventOrchestrator eventOrchestrator;

    /**
     * 上报事件并按订阅关系分发，异常向上抛出。
     *
     * @param reporterId  事件上报器ID
     * @param eventCode   事件编码
     * @param paramValues 事件参数值
     * @return 上报与分发明细
     */
    public EventReportResult report(String reporterId, String eventCode, Collection<ParamValue> paramValues) {
        EventEnvelope envelope = EventEnvelope.builder().reporterId(reporterId).eventCode(eventCode)
            .paramValues(paramValues == null ? List.of() : new ArrayList<>(paramValues)).build();
        return eventOrchestrator.report(envelope);
    }

    /**
     * 上报事件，异常被捕获并降级为失败回执，不向业务调用方抛出。
     *
     * @param reporterId  事件上报器ID
     * @param eventCode   事件编码
     * @param paramValues 事件参数值
     * @return 上报与分发明细；异常时返回失败回执
     */
    public EventReportResult reportQuietly(String reporterId, String eventCode,
        Collection<ParamValue> paramValues) {
        long start = System.currentTimeMillis();
        try {
            return report(reporterId, eventCode, paramValues);
        } catch (Exception e) {
            log.warn("事件上报失败, reporterId={}, eventCode={}, 参数数量={}, 耗时={}ms, 原因={}", reporterId, eventCode,
                paramValues == null ? 0 : paramValues.size(), System.currentTimeMillis() - start, e.getMessage(), e);
            return EventReportResult.builder().reported(false).success(false).errorMsg(e.getMessage())
                .elapsedMs(System.currentTimeMillis() - start).build();
        }
    }

    /**
     * 以键值对形式上报事件，异常被捕获并降级为失败回执。
     *
     * @param reporterId 事件上报器ID
     * @param eventCode  事件编码
     * @param params     事件参数，键为参数名
     * @return 上报与分发明细；异常时返回失败回执
     */
    public EventReportResult reportQuietly(String reporterId, String eventCode, Map<String, Object> params) {
        return reportQuietly(reporterId, eventCode, toParamValues(params));
    }

    /**
     * 键值对转换为参数值列表，按 Map 迭代顺序生成 sort。
     *
     * @param params 事件参数
     * @return 参数值列表；入参为空时返回空列表
     */
    public List<ParamValue> toParamValues(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return List.of();
        }
        List<ParamValue> paramValues = new ArrayList<>(params.size());
        int sort = 1;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            paramValues.add(ParamValue.builder().type("string").name(entry.getKey()).sort(sort++)
                .value(entry.getValue() == null ? null : String.valueOf(entry.getValue())).build());
        }
        return paramValues;
    }
}
