package com.tbs.common.provider.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.tbs.common.provider.ILazyEntriesContributor;
import com.tbs.common.provider.IObjectResolver;
import com.tbs.common.provider.IRegistrationSource;
import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.provider.ObjectResolveContext;
import com.tbs.common.provider.ObjectSource;
import com.tbs.common.provider.RegistryEntry;
import com.tbs.common.provider.ResolvedObject;
import com.tbs.common.provider.SourceCoordinate;
import com.tbs.common.provider.impl.support.RoutingServiceProxy;

import lombok.extern.slf4j.Slf4j;

/**
 * 存量解析器的注册来源桥接。
 * <p>
 * 将旧 SPI {@link IObjectResolver}（阶段 3 起标 {@code @Deprecated}）适配为新 SPI
 * {@link IRegistrationSource} + {@link ILazyEntriesContributor}：装配时把自身登记为中心的懒贡献者，
 * 不预扫全容器；中心读取某接口且本源尚无条目时，委派存量解析器 {@code resolveAll} 现场解析，
 * 把每个解析结果按其 {@link SourceCoordinate} 转为条目、经 replaceEntriesBySource 整段推送入中心。
 * 解析器抛出的异常在此吞掉并降级为空贡献，不打断中心读取。
 * </p>
 * <p>
 * Spring 来源为静态贡献（每接口首读贡献一次）；Dubbo 来源声明读即重贡献，
 * 其真实重查由解析器的快照时效机制节流，扩容/下线在一个刷新间隔内被后续读取发现。
 * 路由代理实例不入中心（通道非实现，中心亦会拒绝，桥接侧先行过滤省开销）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 * @see IRegistrationSource
 * @see ILazyEntriesContributor
 */
@Slf4j
public class ObjectResolverAsSource implements IRegistrationSource, ILazyEntriesContributor {

    /**
     * 被桥接的存量解析器
     */
    private final IObjectResolver resolver;

    /**
     * 是否在中心每次读取时重新贡献（动态来源 true，静态来源 false）
     */
    private final boolean refreshOnRead;

    /**
     * 构造解析器桥接来源
     *
     * @param resolver     存量解析器，不能为null
     * @param refreshOnRead 是否在中心每次读取时重新贡献
     */
    public ObjectResolverAsSource(IObjectResolver resolver, boolean refreshOnRead) {
        this.resolver = resolver;
        this.refreshOnRead = refreshOnRead;
    }

    @Override
    public ObjectSource source() {
        return resolver.getSource();
    }

    @Override
    public void contribute(IRegistryCenter center) {
        if (center instanceof DefaultRegistryCenter defaultCenter) {
            defaultCenter.addLazyContributor(this);
            return;
        }
        log.warn("[RegistrySource] 中心实现不支持懒贡献登记, 来源[{}]需在中心读取路径外自行刷新条目", source());
    }

    @Override
    public boolean refreshOnRead() {
        return refreshOnRead;
    }

    /**
     * 委派存量解析器现场解析并推送条目（区分枚举视图与路由目标两类角色）
     * <p>
     * {@code resolveAll} 产出逐实例结果（标 enumerable）；{@code resolveRoutableTargets} 产出路由目标结果
     * （标 routable）。同一身份（坐标+实例标识）在两侧都出现时（如 Spring Bean）合并为双角色；
     * Dubbo 的逐实例与集群引用身份不同，分别落为仅枚举、仅路由两类条目。
     * 解析上下文只约束目标类型（business/version 由中心侧过滤），解析异常在此吞掉降级为空贡献。
     * </p>
     *
     * @param center 注册中心，不能为null
     * @param key    目标接口类型，不能为null
     */
    @Override
    public void contributeEntries(IRegistryCenter center, Class<?> key) {
        try {
            var context = ObjectResolveContext.of(key);
            List<ResolvedObject<?>> instances = resolver.resolveAll(context).stream()
                .filter(resolved -> !RoutingServiceProxy.isRoutingProxy(resolved.getInstance()))
                .collect(Collectors.toList());
            List<ResolvedObject<?>> targets = resolver.resolveRoutableTargets(context).stream()
                .filter(resolved -> !RoutingServiceProxy.isRoutingProxy(resolved.getInstance()))
                .collect(Collectors.toList());
            Set<String> enumerableIds = instances.stream().map(this::identityOf).collect(Collectors.toSet());
            Set<String> routableIds = targets.stream().map(this::identityOf).collect(Collectors.toSet());
            Map<String, ResolvedObject<?>> byIdentity = new LinkedHashMap<>();
            instances.forEach(resolved -> byIdentity.putIfAbsent(identityOf(resolved), resolved));
            targets.forEach(resolved -> byIdentity.putIfAbsent(identityOf(resolved), resolved));
            var freshEntries = byIdentity.entrySet().stream()
                .map(entry -> toEntry(key, entry.getValue(),
                    enumerableIds.contains(entry.getKey()), routableIds.contains(entry.getKey())))
                .collect(Collectors.toList());
            center.replaceEntriesBySource(source(), key, freshEntries);
        } catch (Exception e) {
            log.warn("来源[{}]接口[{}]懒贡献失败, 原因: {}", source(), key.getName(), e.getMessage());
        }
    }

    /**
     * 计算解析结果的条目身份键（坐标 + 实例标识），用于枚举集与路由集的同身份合并
     *
     * @param resolved 解析结果，不能为null
     * @return 身份键
     */
    private String identityOf(ResolvedObject<?> resolved) {
        return coordinateOf(resolved) + "#" + resolved.getInstanceKey();
    }

    /**
     * 将单个解析结果转为注册条目并按归属标注角色
     *
     * @param key        目标接口类型，不能为null
     * @param resolved   解析结果，不能为null
     * @param enumerable 是否作为枚举视图条目
     * @param routable   是否作为路由目标条目
     * @return 注册条目
     */
    private RegistryEntry toEntry(Class<?> key, ResolvedObject<?> resolved, boolean enumerable, boolean routable) {
        return DefaultRegistryEntry.ofInstance(key, coordinateOfSourceCoordinate(resolved),
            resolved.getInstanceKey(), getOrder(), resolved.getInstance(), enumerable, routable);
    }

    /**
     * 解析结果的来源坐标：坐标缺省时按来源补全空业务空版本坐标，保证条目身份完整
     *
     * @param resolved 解析结果，不能为null
     * @return 来源坐标
     */
    private SourceCoordinate coordinateOfSourceCoordinate(ResolvedObject<?> resolved) {
        return resolved.getCoordinate() == null
            ? SourceCoordinate.ofSource(resolved.getSource())
            : resolved.getCoordinate();
    }

    /**
     * 身份键中的坐标部分：以「来源:业务:版本」稳定表示
     *
     * @param resolved 解析结果，不能为null
     * @return 坐标字符串
     */
    private String coordinateOf(ResolvedObject<?> resolved) {
        var coordinate = coordinateOfSourceCoordinate(resolved);
        return coordinate.getSource().id() + ':' + coordinate.getBusiness() + ':' + coordinate.getVersion();
    }

    @Override
    public int getOrder() {
        return resolver.getOrder();
    }
}
