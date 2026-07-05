package com.tbs.trace.distributed;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 可序列化的链路追踪节点
 * <p>
 * 用于跨服务传输和持久化存储，替代含有不可序列化字段的TraceObject
 * </p>
 *
 * @author abstergo
 */
@Data
@NoArgsConstructor
public class TraceNode implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 节点traceId
     */
    private String traceId;

    /**
     * 父级traceId
     */
    private String parentTraceId;

    /**
     * 服务名称（所属微服务）
     */
    private String serviceName;

    /**
     * 接口类名（如ISystemLogService）
     */
    private String interfaceName;

    /**
     * 实现类名（如SimpleSystemLogServiceImpl）
     */
    private String className;

    /**
     * 方法名
     */
    private String methodSignature;

    /**
     * 方法参数类型列表（逗号分隔）
     */
    private String paramTypes;

    /**
     * 方法参数摘要（序列化后的简要描述）
     */
    private String argsDescription;

    /**
     * 是否为Dubbo远程调用节点
     */
    private boolean dubboCall;

    /**
     * 线程名称
     */
    private String threadName;

    /**
     * 线程ID
     */
    private Long threadId;

    /**
     * 上下文ID
     */
    private String contextId;

    /**
     * HTTP请求方法（GET/POST等，仅HTTP入口节点有值）
     */
    private String httpMethod;

    /**
     * HTTP请求URL（仅HTTP入口节点有值）
     */
    private String httpUrl;

    /**
     * HTTP响应状态码
     */
    private Integer httpStatus;

    /**
     * 客户端IP地址
     */
    private String remoteAddr;

    /**
     * 客户端User-Agent
     */
    private String userAgent;

    /**
     * HTTP Content-Type
     */
    private String contentType;

    /**
     * 查询字符串参数
     */
    private String queryString;

    /**
     * 异常信息（如果调用过程中抛出异常）
     */
    private String exceptionMessage;

    /**
     * 异常类型
     */
    private String exceptionType;

    /**
     * 节点生效时间
     */
    private Date availableTime;

    /**
     * 节点失效时间
     */
    private Date disposeTime;

    /**
     * 子节点列表
     */
    private List<TraceNode> children = new ArrayList<>();

    public TraceNode(String traceId, String parentTraceId) {
        this.traceId = traceId;
        this.parentTraceId = parentTraceId;
    }

    /**
     * 添加子节点
     */
    public void addChild(TraceNode child) {
        this.children.add(child);
    }

    /**
     * 获取方法的完整签名（接口名.方法名）
     */
    public String getFullSignature() {
        String name = interfaceName != null ? interfaceName : className;
        if (name != null && methodSignature != null) {
            return name + "." + methodSignature;
        }
        return methodSignature;
    }

    /**
     * 计算节点耗时（毫秒）
     */
    public long getDurationMs() {
        if (availableTime != null && disposeTime != null) {
            return disposeTime.getTime() - availableTime.getTime();
        }
        return -1;
    }

    /**
     * 将树格式化为可读的文本调用链
     * <pre>
     * [app-service] 46071d92 (31ms) http:GET /api/xxx
     * └── ISystemLogService.getLogOperateTypeList (ba716691, dubbo, 388ms) http-nio-8080-exec-1
     *     └── ISystemLogService.getLogOperateTypeList (e6a093f0, 82ms) dubbo-thread-5
     *         └── SimpleSystemLogServiceImpl.getLogOperateTypeList (2a628c1e, 79ms)
     *             └── DbLogExtractor.extract (e49d2722, 6ms)
     * </pre>
     */
    public String formatTree() {
        StringBuilder sb = new StringBuilder();
        formatTree(sb, "", true);
        return sb.toString();
    }

    private void formatTree(StringBuilder sb, String prefix, boolean isLast) {
        String connector = isLast ? "└── " : "├── ";
        String duration = getDurationMs() >= 0 ? " (" + getDurationMs() + "ms)" : "";
        String shortId = traceId != null && traceId.length() > 8 ? traceId.substring(0, 8) : traceId;
        String signature = getFullSignature();

        if (signature != null) {
            // 方法节点
            StringBuilder line = new StringBuilder();
            line.append(prefix).append(connector).append(signature)
                .append(" (").append(shortId);
            if (dubboCall) {
                line.append(", dubbo");
            }
            line.append(")").append(duration);

            // 线程信息
            if (threadName != null) {
                line.append(" @").append(threadName);
            }

            // HTTP信息
            if (httpMethod != null) {
                line.append(" ").append(httpMethod).append(" ").append(httpUrl);
            }

            // 参数类型
            if (paramTypes != null && !paramTypes.isEmpty()) {
                line.append("\n").append(prefix).append(isLast ? "    " : "│   ")
                    .append("  args: (").append(paramTypes).append(")");
            }

            // 参数值摘要
            if (argsDescription != null && !argsDescription.isEmpty()) {
                String desc = argsDescription.length() > 120
                    ? argsDescription.substring(0, 120) + "..."
                    : argsDescription;
                line.append("\n").append(prefix).append(isLast ? "    " : "│   ")
                    .append("  values: ").append(desc);
            }

            // 异常信息
            if (exceptionType != null) {
                String exShort = exceptionType.contains(".")
                    ? exceptionType.substring(exceptionType.lastIndexOf('.') + 1)
                    : exceptionType;
                line.append("\n").append(prefix).append(isLast ? "    " : "│   ")
                    .append("  ! ").append(exShort);
                if (exceptionMessage != null) {
                    String msg = exceptionMessage.length() > 100
                        ? exceptionMessage.substring(0, 100) + "..."
                        : exceptionMessage;
                    line.append(": ").append(msg);
                }
            }

            sb.append(line).append("\n");
        } else {
            // 根节点
            String label = serviceName != null ? "[" + serviceName + "] " : "";
            sb.append(label).append(shortId).append(duration);
            if (httpMethod != null) {
                sb.append(" ").append(httpMethod).append(" ").append(httpUrl);
            }
            if (httpStatus != null) {
                sb.append(" [").append(httpStatus).append("]");
            }
            sb.append("\n");
            // 根节点附加信息
            String childPrefix = "    ";
            if (remoteAddr != null) {
                sb.append(childPrefix).append("client: ").append(remoteAddr);
                if (userAgent != null) {
                    String ua = userAgent.length() > 80 ? userAgent.substring(0, 80) + "..." : userAgent;
                    sb.append(" | ").append(ua);
                }
                sb.append("\n");
            }
            if (queryString != null && !queryString.isEmpty()) {
                sb.append(childPrefix).append("params: ").append(queryString).append("\n");
            }
            if (threadName != null) {
                sb.append(childPrefix).append("thread: ").append(threadName).append(" #").append(threadId).append("\n");
            }
        }

        if (children != null && !children.isEmpty()) {
            String childPrefix = prefix + (isLast ? "    " : "│   ");
            for (int i = 0; i < children.size(); i++) {
                children.get(i).formatTree(sb, childPrefix, i == children.size() - 1);
            }
        }
    }
}
