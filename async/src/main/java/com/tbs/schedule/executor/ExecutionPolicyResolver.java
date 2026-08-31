package com.tbs.schedule.executor;

import com.tbs.common.util.framework.SpringUtil;
import com.tbs.schedule.api.ExecutionPolicy;
import com.tbs.schedule.api.TaskSubmission;
import com.tbs.schedule.api.TypedExecutionPolicy;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 执行策略解析器
 * <p>
 * 将"单任务提交指定 &gt; 按任务类型配置 &gt; 全局默认"三级覆盖解析为最终生效的 {@link ExecutionPolicy}。 解析模式与 cache 包
 * {@code ICacheTtlPolicy.resolveBusinessTtl} 一致，沿用本仓库既有风格。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class ExecutionPolicyResolver {

    /**
     * 全局默认策略
     */
    private final ExecutionPolicy globalDefault;

    /**
     * 按任务类型覆盖的策略
     */
    private final Map<String, ExecutionPolicy> perType;

    /**
     * 构造策略解析器
     *
     * @param globalDefault 全局默认策略（不可为 null）
     * @param perType       按任务类型的策略覆盖（可为 null）
     */
    public ExecutionPolicyResolver(ExecutionPolicy globalDefault, Map<String, ExecutionPolicy> perType) {
        this.globalDefault = globalDefault;
        this.perType = perType == null ? new HashMap<>() : perType;
    }

    /**
     * 解析任务最终生效的执行策略
     * <p>
     * 优先级：单任务提交指定 &gt; 按任务类型配置 &gt; 全局默认。
     * </p>
     *
     * @param submission 任务提交描述
     * @return 最终生效策略
     */
    @NotNull
    public ExecutionPolicy resolve(TaskSubmission submission) {
        return submission.getPolicyOverride().orElse(perType.getOrDefault(submission.getTaskType(), globalDefault));
    }

    /**
     * 从 Spring 容器收集全局默认与按类型策略，构建解析器
     *
     * @return 解析器
     */
    public static ExecutionPolicyResolver fromSpring() {
        Collection<ExecutionPolicy> all = SpringUtil.getBeansByTypeSafe(ExecutionPolicy.class);
        ExecutionPolicy global = all.stream().filter(p -> !(p instanceof TypedExecutionPolicy)).findFirst()
            .orElseThrow(() -> new IllegalStateException("no global ExecutionPolicy bean"));
        Map<String, ExecutionPolicy> perType = new HashMap<>();
        all.forEach(p -> {
            if (p instanceof TypedExecutionPolicy) {
                var t = (TypedExecutionPolicy)p;
                perType.put(t.getTaskType(), t);
            }
        });
        return new ExecutionPolicyResolver(global, perType);
    }
}
