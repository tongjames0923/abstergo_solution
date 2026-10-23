package com.tbs.common.provider;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;

/**
 * 路由策略（值对象）：对来源偏好的有序表达。
 * <p>
 * 由枚举改为携带 {@link List}&lt;{@link ObjectSource}&gt; 优先序的值对象；保留 4 个内置常量与
 * accept/primarySource/backupSource/name 兼容方法，使存量 RoutingServiceProxy 调用点行为不变。
 * {@link #custom(ObjectSource...)} 让第三方来源参与路由偏好，无需改动中心。
 * </p>
 * <p>
 * 注解 {@code @RoutingService.strategy()} 以策略名字符串承载（注解元素不可为对象类型），经 {@link #byName(String)} 还原。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class RoutingStrategy {

    /**
     * 本地优先：Spring 首选，Dubbo 兜底
     */
    public static final RoutingStrategy LOCAL_FIRST = named("LOCAL_FIRST", ObjectSource.SPRING, ObjectSource.DUBBO);

    /**
     * 远程优先：Dubbo 首选，Spring 兜底
     */
    public static final RoutingStrategy REMOTE_FIRST = named("REMOTE_FIRST", ObjectSource.DUBBO, ObjectSource.SPRING);

    /**
     * 仅本地：只接受 Spring 来源
     */
    public static final RoutingStrategy SPRING_ONLY = named("SPRING_ONLY", ObjectSource.SPRING);

    /**
     * 仅远程：只接受 Dubbo 来源
     */
    public static final RoutingStrategy DUBBO_ONLY = named("DUBBO_ONLY", ObjectSource.DUBBO);

    /**
     * 可读策略名，供日志、i18n 参数与缓存键使用；内置常量为固定大写名，自定义来源偏好由 id 序派生
     */
    private final String name;

    /**
     * 来源优先序，越靠前优先级越高
     */
    private final List<ObjectSource> preference;

    private RoutingStrategy(String name, List<ObjectSource> preference) {
        this.name = name;
        this.preference = List.copyOf(preference);
    }

    /**
     * 按优先序构建策略
     *
     * @param orderedSources 来源优先序，越靠前优先级越高，不能为空
     * @return 路由策略
     */
    public static RoutingStrategy of(ObjectSource... orderedSources) {
        return named(deriveName(orderedSources), orderedSources);
    }

    /**
     * 自定义来源优先序（第三方来源接入点）
     *
     * @param orderedSources 任意来源组合，含第三方来源
     * @return 路由策略
     */
    public static RoutingStrategy custom(ObjectSource... orderedSources) {
        return of(orderedSources);
    }

    /**
     * 按策略名还原内置策略，供注解字符串值还原
     *
     * @param name 策略名，取值为四个内置常量名之一
     * @return 路由策略；未知名快速失败
     */
    public static RoutingStrategy byName(String name) {
        RoutingStrategy strategy = switch (name == null ? "" : name) {
            case "LOCAL_FIRST" -> LOCAL_FIRST;
            case "REMOTE_FIRST" -> REMOTE_FIRST;
            case "SPRING_ONLY" -> SPRING_ONLY;
            case "DUBBO_ONLY" -> DUBBO_ONLY;
            default -> null;
        };
        AssertUtil.isTrue(strategy == null).i18n(CommonI18nConstant.REGISTRY_STRATEGY_UNKNOWN)
            .params(name).errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        return strategy;
    }

    /**
     * 以显式策略名构建，校验优先序非空
     *
     * @param name          策略名，不能为null
     * @param orderedSources 来源优先序，不能为空
     * @return 路由策略
     */
    private static RoutingStrategy named(String name, ObjectSource... orderedSources) {
        AssertUtil.isTrue(orderedSources == null || orderedSources.length == 0)
            .i18n(CommonI18nConstant.REGISTRY_STRATEGY_PREFERENCE_EMPTY)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        return new RoutingStrategy(name, List.of(orderedSources));
    }

    /**
     * 由来源优先序派生可读名（各来源 id 以 &gt; 连接）
     *
     * @param orderedSources 来源优先序
     * @return 派生策略名
     */
    private static String deriveName(ObjectSource... orderedSources) {
        if (orderedSources == null || orderedSources.length == 0) {
            return "";
        }
        return Arrays.stream(orderedSources).map(ObjectSource::id).collect(Collectors.joining(">"));
    }

    /**
     * 判断策略是否接受该来源（兼容原枚举方法）
     *
     * @param source 待判定来源，不能为null
     * @return true 表示该来源可纳入路由选择
     */
    public boolean accept(ObjectSource source) {
        return preference.contains(source);
    }

    /**
     * 首选来源（兼容原枚举方法）
     *
     * @return preference 首项
     */
    public ObjectSource primarySource() {
        return preference.get(0);
    }

    /**
     * 降级来源（兼容原枚举方法）：无降级时返回与首选相同项，accept 将据偏好过滤
     *
     * @return preference 第二项或首项
     */
    public ObjectSource backupSource() {
        return preference.size() > 1 ? preference.get(1) : preference.get(0);
    }

    /**
     * 兼容原枚举 name()：返回可读策略名（供日志与 i18n 参数）
     *
     * @return 策略名
     */
    public String name() {
        return name;
    }

    /**
     * 来源优先序（只读）
     *
     * @return 优先序列表
     */
    public List<ObjectSource> preference() {
        return preference;
    }

    /**
     * 按策略来源偏好重排候选条目：先比来源在 preference 中的位次，同来源内比 entry.order（降序）
     *
     * @param entries 候选条目，不能为null
     * @return 重排后的条目列表，仅保留策略接受的来源
     */
    public List<RegistryEntry> reorder(List<RegistryEntry> entries) {
        return entries.stream()
            .filter(entry -> accept(entry.coordinate().getSource()))
            .sorted(Comparator
                .comparingInt((RegistryEntry entry) -> preference.indexOf(entry.coordinate().getSource()))
                .thenComparingInt(entry -> -entry.order()))
            .collect(Collectors.toList());
    }

    /**
     * 等值基于来源优先序
     *
     * @param o 待比较对象
     * @return true 表示偏好序一致
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RoutingStrategy)) {
            return false;
        }
        return Objects.equals(preference, ((RoutingStrategy) o).preference);
    }

    /**
     * 哈希基于来源优先序，与 equals 一致
     *
     * @return 哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(preference);
    }

    /**
     * 返回策略名，替代原枚举 toString
     *
     * @return 策略名
     */
    @Override
    public String toString() {
        return name;
    }
}
