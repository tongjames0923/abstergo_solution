package com.tbs.script.api.expressible.impl;

import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.util.base.StrUtil;
import com.tbs.script.api.expressible.ITrigger;
import com.tbs.script.api.expressible.vo.TriggerMeta;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 触发器元信息查询服务。
 *
 * <p>供前端订阅编排页列出可选触发器（如 {@code ConditionRuleTrigger}）并读取其参数声明；
 * 实例统一经 {@link IRegistryCenter} 实时取用，不做缓存，运行时注册/注销立即生效。</p>
 *
 * @author abstergo
 */
@DubboService
@Slf4j
@Service
public class TriggerQueryService {

    /**
     * 注册中心，用于定位触发器实例
     */
    @Resource
    private IRegistryCenter registryCenter;

    /**
     * 列出全部触发器元信息，按触发器ID字典序排列。
     *
     * @return 触发器元信息列表；无注册实例时返回空列表
     */
    public List<TriggerMeta> listTriggers() {
        return triggers().stream().map(this::toMeta)
            .sorted(Comparator.comparing(TriggerMeta::getTriggerId, Comparator.nullsLast(String::compareTo)))
            .collect(Collectors.toList());
    }

    /**
     * 按ID获取触发器元信息。
     *
     * @param triggerId 触发器ID
     * @return 触发器元信息；未注册时返回 null
     */
    public TriggerMeta getTrigger(String triggerId) {
        ITrigger trigger = getTriggerInstance(triggerId);
        return trigger == null ? null : toMeta(trigger);
    }

    /**
     * 按ID获取触发器实例，供后端运行期或调试场景直接调用。
     *
     * @param triggerId 触发器ID
     * @return 触发器实例；未注册时返回 null
     */
    public ITrigger getTriggerInstance(String triggerId) {
        if (StrUtil.isBlank(triggerId)) {
            return null;
        }
        return triggers().stream().filter(trigger -> triggerId.equals(trigger.getTriggerId())).findFirst().orElse(null);
    }

    /**
     * 元信息映射，读取失败的展示字段降级为 null，不影响列表返回。
     *
     * @param trigger 触发器实例
     * @return 触发器元信息
     */
    private TriggerMeta toMeta(ITrigger trigger) {
        return TriggerMeta.builder().triggerId(trigger.getTriggerId()).triggerName(safeText(trigger::getTriggerName))
            .description(safeText(trigger::getDescription)).paramTypes(trigger.requestParamTypes()).build();
    }

    /**
     * 安全读取展示文案，读取失败返回 null。
     *
     * @param supplier 文案读取逻辑
     * @return 文案内容或 null
     */
    private String safeText(Supplier<String> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从注册中心加载全部触发器实例，按触发器ID去重（先注册者优先）。
     *
     * @return 触发器实例列表
     */
    private List<ITrigger> triggers() {
        Map<String, ITrigger> resolved = new LinkedHashMap<>();
        registryCenter.getAllContributors().stream()
            .flatMap(contributor -> contributor
                .getAllInstanceByTypeAndBusinessAndVersion(ITrigger.class, null, null).stream())
            .map(instance -> (ITrigger)instance.getInstance()).filter(Objects::nonNull)
            .filter(trigger -> StrUtil.isNotBlank(trigger.getTriggerId()))
            .forEach(trigger -> resolved.putIfAbsent(trigger.getTriggerId(), trigger));
        return new ArrayList<>(resolved.values());
    }
}
