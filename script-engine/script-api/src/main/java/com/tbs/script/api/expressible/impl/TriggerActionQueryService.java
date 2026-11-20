package com.tbs.script.api.expressible.impl;

import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.util.base.StrUtil;
import com.tbs.script.api.expressible.ITriggerAction;
import com.tbs.script.api.expressible.constant.ScriptI18nConstant;
import com.tbs.script.api.expressible.vo.ActionMeta;
import com.tbs.script.api.expressible.vo.param.ParamValue;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 触发器动作元信息查询服务。
 *
 * <p>供前端动作编排页列出可选动作并读取参数声明，同时提供动作实例定位与单次执行能力，
 * 供后端运行期与调试接口复用。实例统一经 {@link IRegistryCenter} 实时取用，不做缓存。</p>
 *
 * @author abstergo
 */
@DubboService
@Slf4j
@Service
public class TriggerActionQueryService {

    /**
     * 注册中心，用于定位动作实例
     */
    @Resource
    private IRegistryCenter registryCenter;

    /**
     * 列出全部动作元信息，按声明顺序升序、动作ID字典序兜底。
     *
     * @return 动作元信息列表；无注册实例时返回空列表
     */
    public List<ActionMeta> listActions() {
        return actions().stream().map(this::toMeta)
            .sorted(Comparator.comparing(ActionMeta::getOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(ActionMeta::getActionId, Comparator.nullsLast(String::compareTo)))
            .collect(Collectors.toList());
    }

    /**
     * 按ID获取动作元信息。
     *
     * @param actionId 动作ID
     * @return 动作元信息；未注册时返回 null
     */
    public ActionMeta getAction(String actionId) {
        ITriggerAction action = getActionInstance(actionId);
        return action == null ? null : toMeta(action);
    }

    /**
     * 按ID获取动作实例。
     *
     * @param actionId 动作ID
     * @return 动作实例；未注册时返回 null
     */
    public ITriggerAction getActionInstance(String actionId) {
        if (StrUtil.isBlank(actionId)) {
            return null;
        }
        return actions().stream().filter(action -> actionId.equals(action.getActionId())).findFirst().orElse(null);
    }

    /**
     * 执行一次动作，异常被捕获并转为失败回执，不向调用方抛出。
     *
     * @param actionId    动作ID
     * @param paramValues 动作参数值
     * @return true 执行成功；动作不存在或执行异常时返回 false
     */
    public boolean execute(String actionId, Collection<ParamValue> paramValues) {
        ITriggerAction action = getActionInstance(actionId);
        if (action == null) {
            log.warn("动作执行失败, {}", I18nUtil.get(ScriptI18nConstant.TRIGGER_ACTION_NOT_FOUND, actionId));
            return false;
        }
        try {
            action.execute(paramValues);
            return true;
        } catch (Exception e) {
            log.warn("动作执行异常, actionId={}, 原因={}", actionId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 元信息映射，读取失败的展示字段降级为 null，不影响列表返回。
     *
     * @param action 动作实例
     * @return 动作元信息
     */
    private ActionMeta toMeta(ITriggerAction action) {
        return ActionMeta.builder().actionId(action.getActionId()).actionName(safeText(action::getActionName))
            .actionDesc(safeText(action::getActionDesc)).order(safeOrder(action))
            .paramTypes(action.requestParamTypes()).build();
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
     * 安全读取动作默认顺序，读取失败返回 null。
     *
     * @param action 动作实例
     * @return 默认顺序或 null
     */
    private Integer safeOrder(ITriggerAction action) {
        try {
            return action.getOrder();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从注册中心加载全部动作实例，按动作ID去重（先注册者优先）。
     *
     * @return 动作实例列表
     */
    private List<ITriggerAction> actions() {
        Map<String, ITriggerAction> resolved = new LinkedHashMap<>();
        registryCenter.getAllContributors().stream()
            .flatMap(contributor -> contributor
                .getAllInstanceByTypeAndBusinessAndVersion(ITriggerAction.class, null, null).stream())
            .map(instance -> (ITriggerAction)instance.getInstance()).filter(Objects::nonNull)
            .filter(action -> StrUtil.isNotBlank(action.getActionId()))
            .forEach(action -> resolved.putIfAbsent(action.getActionId(), action));
        return new ArrayList<>(resolved.values());
    }
}
