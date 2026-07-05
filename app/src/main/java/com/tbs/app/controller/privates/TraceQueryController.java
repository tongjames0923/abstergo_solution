package com.tbs.app.controller.privates;

import com.tbs.shared.constant.RoleConstants;
import com.tbs.trace.distributed.DistributedTraceAggregator;
import com.tbs.trace.distributed.TraceNode;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 链路追踪查询控制器
 *
 * @author abstergo
 */
@RestController
@RequestMapping("/private/trace")
@PreAuthorize(
    "hasAnyAuthority('" + RoleConstants.SUPER_ADMIN_ROLE_NAME + "','" + RoleConstants.DEVELOPER_ROLE_NAME + "')")
@Tag(name = "链路追踪", description = "分布式链路追踪查询接口")
public class TraceQueryController extends BaseController {

    @Resource
    private DistributedTraceAggregator distributedTraceAggregator;

    @GetMapping("list")
    @Operation(summary = "获取所有traceId", description = "获取所有已记录的rootTraceId列表")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "成功获取列表"))
    public AjaxResult listTraceIds() {
        return successResult(distributedTraceAggregator.listTraceIds());
    }

    @GetMapping("tree")
    @Operation(summary = "查询调用树", description = "根据rootTraceId查询完整的分布式调用树JSON")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "成功查询调用树"))
    public AjaxResult getTraceTree(
        @Parameter(name = "rootTraceId", description = "根traceId", required = true) @RequestParam String rootTraceId) {
        Optional<TraceNode> trace = distributedTraceAggregator.getDistributedTrace(rootTraceId);
        if (trace.isEmpty()) {
            return AjaxUtil.getInstance().success("未找到对应的trace记录", null);
        }
        return successResult(trace.get());
    }

    @GetMapping("tree/text")
    @Operation(summary = "查询调用链文本", description = "根据rootTraceId查询格式化的树形调用链文本")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "成功查询调用链"))
    public AjaxResult getTraceTreeText(
        @Parameter(name = "rootTraceId", description = "根traceId", required = true) @RequestParam String rootTraceId) {
        Optional<TraceNode> trace = distributedTraceAggregator.getDistributedTrace(rootTraceId);
        if (trace.isEmpty()) {
            return AjaxUtil.getInstance().success("未找到对应的trace记录", null);
        }
        TraceNode root = trace.get();
        Map<String, Object> result = new HashMap<>();
        result.put("rootTraceId", rootTraceId);
        result.put("tree", root);
        result.put("text", root.formatTree());
        return successResult(result);
    }

    @GetMapping("tree/summary")
    @Operation(summary = "查询调用链摘要", description = "根据rootTraceId查询调用链的关键指标摘要")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "成功查询摘要"))
    public AjaxResult getTraceSummary(
        @Parameter(name = "rootTraceId", description = "根traceId", required = true) @RequestParam String rootTraceId) {
        Optional<TraceNode> trace = distributedTraceAggregator.getDistributedTrace(rootTraceId);
        if (trace.isEmpty()) {
            return AjaxUtil.getInstance().success("未找到对应的trace记录", null);
        }
        TraceNode root = trace.get();
        Map<String, Object> summary = new HashMap<>();
        summary.put("rootTraceId", rootTraceId);
        summary.put("totalDurationMs", root.getDurationMs());
        summary.put("nodeCount", countNodes(root));
        summary.put("dubboCallCount", countDubboCalls(root));
        summary.put("maxDepth", calcDepth(root));
        summary.put("tree", root);
        summary.put("text", root.formatTree());
        return successResult(summary);
    }

    private int countNodes(TraceNode node) {
        if (node == null) {
            return 0;
        }
        int count = 1;
        if (node.getChildren() != null) {
            for (TraceNode child : node.getChildren()) {
                count += countNodes(child);
            }
        }
        return count;
    }

    private int countDubboCalls(TraceNode node) {
        if (node == null) {
            return 0;
        }
        int count = node.isDubboCall() ? 1 : 0;
        if (node.getChildren() != null) {
            for (TraceNode child : node.getChildren()) {
                count += countDubboCalls(child);
            }
        }
        return count;
    }

    private int calcDepth(TraceNode node) {
        if (node == null || node.getChildren() == null || node.getChildren().isEmpty()) {
            return 0;
        }
        int maxChildDepth = 0;
        for (TraceNode child : node.getChildren()) {
            maxChildDepth = Math.max(maxChildDepth, calcDepth(child));
        }
        return maxChildDepth + 1;
    }

    @Override
    public String getBusinessName() {
        return "链路追踪";
    }
}
