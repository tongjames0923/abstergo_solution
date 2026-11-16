package com.tbs.sql.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mapper 接口与实体类型的绑定注册表。
 *
 * <p>{@link com.tbs.sql.annotation.core.SqlEntityScan} 在启动期扫描到实体后，把所有
 * {@link IFrameworkMapper} Bean 的实体泛型解析出来写入这里，并校验实体是否落在扫描范围内，
 * 配置错误在容器刷新阶段即暴露。运行期 {@link IFrameworkMapper#query()} 直接按 Mapper 接口
 * 查表得到实体类型，避免每次调用都反射遍历接口继承链。</p>
 *
 * <p>未启用 {@code @SqlEntityScan} 或扫描范围为空时不做校验；{@link #getOrResolve(Class)}
 * 对未绑定的 Mapper 仍可按泛型惰性解析并缓存，保持原有无注解场景的行为。</p>
 *
 * @author abstergo
 * @since 3.0.0
 */
public final class MapperEntityBindingRegistry {

    private static final Logger log = LoggerFactory.getLogger(MapperEntityBindingRegistry.class);
    private static final Map<Class<?>, Class<?>> BINDINGS = new ConcurrentHashMap<>(64);
    private static volatile Set<Class<?>> scannedEntities = Set.of();
    private static volatile boolean validationEnabled;

    private MapperEntityBindingRegistry() {
    }

    /**
     * 批量绑定 Mapper 接口与实体类型，并在启用校验时验证实体是否已被扫描。
     *
     * @param mapperTypes 容器内的 Mapper 接口类型
     * @param scanned     {@code @SqlEntityScan} 已扫描并校验通过的实体集合
     * @param validate    是否执行“实体必须在扫描范围内”的校验
     * @throws IllegalStateException 存在无法解析或未扫描的 Mapper 时抛出，汇总列出全部问题
     */
    public static void bindAll(Collection<Class<?>> mapperTypes, Set<Class<?>> scanned, boolean validate) {
        BINDINGS.clear();
        scannedEntities = scanned == null || scanned.isEmpty() ? Set.of() : Set.copyOf(scanned);
        validationEnabled = validate && !scannedEntities.isEmpty();
        if (mapperTypes == null || mapperTypes.isEmpty()) {
            return;
        }

        Map<Class<?>, String> problems = new LinkedHashMap<>();
        for (Class<?> mapperType : mapperTypes) {
            if (mapperType == null || !IFrameworkMapper.class.isAssignableFrom(mapperType)) {
                continue;
            }
            Class<?> entityClass = MapperEntityTypeResolver.resolveEntityClass(mapperType);
            if (entityClass == null) {
                problems.put(mapperType, "无法解析Mapper接口声明的实体泛型，请确认其继承IFrameworkMapper/GenericMapper并声明实体类型");
                continue;
            }
            if (validationEnabled && !scannedEntities.contains(entityClass)) {
                problems.put(mapperType, "实体" + entityClass.getName() + "未纳入@SqlEntityScan扫描范围");
                continue;
            }
            BINDINGS.put(mapperType, entityClass);
        }

        if (!problems.isEmpty()) {
            problems.keySet().forEach(BINDINGS::remove);
            StringBuilder message = new StringBuilder("Mapper与实体绑定校验失败，共")
                .append(problems.size()).append("个Mapper：");
            problems.forEach((mapperType, reason) ->
                message.append("\n  - ").append(mapperType.getName()).append("：").append(reason));
            throw new IllegalStateException(message.toString());
        }
        log.info("@SqlEntityScan 已完成 {} 个 Mapper 与实体的绑定，实体校验={}", BINDINGS.size(), validationEnabled);
    }

    /**
     * 取 Mapper 接口对应的实体类型；未绑定时按泛型惰性解析并缓存。
     *
     * @param mapperType Mapper 接口类型
     * @return 实体类型
     * @throws IllegalStateException Mapper 类型为空或无法解析实体泛型时抛出
     */
    public static Class<?> getOrResolve(Class<?> mapperType) {
        if (mapperType == null) {
            throw new IllegalStateException("无法解析当前对象对应的Mapper接口类型");
        }
        Class<?> bound = BINDINGS.get(mapperType);
        if (bound != null) {
            return bound;
        }
        Class<?> resolved = MapperEntityTypeResolver.resolveEntityClass(mapperType);
        if (resolved == null) {
            throw new IllegalStateException("无法解析Mapper接口" + mapperType.getName()
                + "的实体类型，请确认其继承IFrameworkMapper/GenericMapper并声明实体泛型");
        }
        Class<?> raced = BINDINGS.putIfAbsent(mapperType, resolved);
        return raced != null ? raced : resolved;
    }

    /**
     * 已扫描并被绑定的实体集合，仅供诊断使用。
     *
     * @return 实体类型集合的不可变快照
     */
    public static Set<Class<?>> scannedEntities() {
        return scannedEntities;
    }

    /**
     * 清空注册表，仅供单元测试隔离使用。
     */
    static void clear() {
        BINDINGS.clear();
        scannedEntities = Set.of();
        validationEnabled = false;
    }
}
