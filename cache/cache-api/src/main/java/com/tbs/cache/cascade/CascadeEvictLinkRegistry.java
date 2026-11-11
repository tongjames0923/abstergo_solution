package com.tbs.cache.cascade;

import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 关联失效声明注册表
 * <p>
 * 收集容器内全部 {@link ICacheEvictLink} 声明，按源缓存名称建立索引，并在初始化时做声明合法性校验。
 * 首次使用时懒加载（与 {@code AbstractAdaptTtlCache} 收集监听器的方式一致），避免参与缓存管理器的
 * 装配期依赖。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
public class CascadeEvictLinkRegistry {

    /**
     * 源缓存名称 → 关联声明列表；懒加载后不可变
     */
    private volatile Map<String, List<ICacheEvictLink>> linkIndex;

    /**
     * 获取指定缓存被清除时的关联声明
     *
     * @param cacheName 缓存名称
     * @return 关联声明列表，无声明时返回空列表
     */
    public List<ICacheEvictLink> linksOf(String cacheName) {
        if (StrUtil.isEmpty(cacheName)) {
            return Collections.emptyList();
        }
        return indexOf().getOrDefault(cacheName, Collections.emptyList());
    }

    private Map<String, List<ICacheEvictLink>> indexOf() {
        Map<String, List<ICacheEvictLink>> current = linkIndex;
        if (current == null) {
            synchronized (this) {
                current = linkIndex;
                if (current == null) {
                    current = build();
                    linkIndex = current;
                }
            }
        }
        return current;
    }

    /**
     * 建索引并校验声明合法性
     * <p>
     * 级联执行走静默清除，因此不会递归触发，运行期不可能成环；这里的校验只处理两类声明错误：
     * 自环（清自己，无意义）与「目标自身又声明了源」。后者必须告警——静默清除意味着<b>级联不传递</b>，
     * 写了 {@code A→B} 与 {@code B→C} 时，清除 A 只会连带清除 B，不会继续清除 C，
     * 多级依赖须由声明方展开为 {@code A → {B, C}}。丢弃声明反而会抹掉 {@code B→C} 在直接清除 B
     * 时的正确行为，所以这里只警告不剔除。
     * </p>
     */
    private Map<String, List<ICacheEvictLink>> build() {
        List<ICacheEvictLink> links = collect();
        Set<String> sources = new HashSet<>(Math.max(8, links.size()));
        for (ICacheEvictLink link : links) {
            if (StrUtil.isNotEmpty(link.source())) {
                sources.add(link.source());
            }
        }

        Map<String, List<ICacheEvictLink>> index = new HashMap<>(Math.max(8, links.size()));
        Set<String> edges = new HashSet<>(Math.max(8, links.size()));
        for (ICacheEvictLink link : links) {
            String source = link.source();
            String target = link.target();
            if (StrUtil.isEmpty(source) || StrUtil.isEmpty(target)) {
                log.error("关联失效声明[{}]缺少源缓存或目标缓存，已忽略", link.getName());
                continue;
            }
            if (source.equals(target)) {
                log.error("关联失效声明[{}]把自身声明为目标（自环），已忽略该声明", link.getName());
                continue;
            }
            if (!edges.add(source + " -> " + target)) {
                log.warn("关联失效声明[{}]与已有声明重复：{} -> {}，按一条处理", link.getName(), source, target);
            }
            if (sources.contains(target)) {
                log.warn("关联失效声明[{}]的目标[{}]自身还声明了关联，级联不传递：清除[{}]不会连带清除"
                    + "[{}]的关联，请把多级依赖显式展开", link.getName(), target, source, target);
            }
            index.computeIfAbsent(target, key -> new ArrayList<>(4)).add(link);
        }
        log.info("关联失效声明装载完成：声明数={}, 源缓存数={}", links.size(), index.size());
        return Collections.unmodifiableMap(index);
    }

    private List<ICacheEvictLink> collect() {
        try {
            return new ArrayList<>(SpringUtil.getBeansOfType(ICacheEvictLink.class).values());
        } catch (Exception e) {
            log.error("收集关联失效声明失败，级联失效不可用", e);
            return new ArrayList<>();
        }
    }
}
