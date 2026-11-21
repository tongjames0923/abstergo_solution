package com.tbs.cache.integration;

import com.tbs.cache.IMultiLevelSupport;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * 多层缓存编排抽象基类
 * <p>
 * 纯编排职责，不包含 TTL 能力（由装饰层 {@link AbstractAdaptTtlCache} 包装提供）。 核心语义：
 * <ul>
 *   <li><b>读取</b>：按层级顺序穿透（L1 → L2 → ...），命中后静默回填上层</li>
 *   <li><b>写入</b>：自底向上写入（L2 → L1），远层失败快速失败</li>
 *   <li><b>失效</b>：全层失效，尽力而为</li>
 * </ul>
 * </p>
 * <p>
 * 回填与写入的语义区别：
 * <ul>
 *   <li>回填（backfill）：读穿透命中下层后静默同步上层，不触发监听器事件</li>
 *   <li>写入（write-through）：应用层主动写入，事件由装饰层触发</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractMultiLevelCache implements Cache, IMultiLevelSupport {

    /**
     * 缓存层级列表（按 order 升序排列，index 0 为最快层）
     */
    protected final List<Cache> levels;

    /**
     * 缓存名称
     */
    protected final String name;

    /**
     * 构造函数
     *
     * @param name   缓存名称
     * @param levels 缓存层级列表（已按 order 排序）
     */
    protected AbstractMultiLevelCache(String name, List<Cache> levels) {
        if (levels == null || levels.isEmpty()) {
            AssertUtil.throwError(() ->
                new IllegalArgumentException(I18nUtil.get("cache.level.required")));
        }
        this.name = name;
        this.levels = Collections.unmodifiableList(new ArrayList<>(levels));

        String levelSummary = levels.stream().map(c -> c.getClass().getSimpleName() + "(" + c.getName() + ")")
            .collect(Collectors.joining(", "));
        log.info("Multi-level cache created: name={}, levelCount={}, levels=[{}]", name, levels.size(), levelSummary);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        // 返回最快层的原生缓存作为代表
        return levels.get(0).getNativeCache();
    }

    /**
     * 获取缓存层级列表
     *
     * @return 不可变的层级列表
     */
    @Override
    public List<Cache> getLevels() {
        return levels;
    }

    @Override
    public ValueWrapper get(Object key) {
        for (int i = 0; i < levels.size(); i++) {
            ValueWrapper value = levels.get(i).get(key);
            if (value != null) {
                log.debug("[{}] Cache hit at L{}: key={}, levelClass={}", name, i + 1, key,
                    levels.get(i).getClass().getSimpleName());
                backfillUpperLevels(i, key, value.get());
                return value;
            }
        }
        log.debug("[{}] Cache miss across all levels: key={}, levelCount={}", name, key, levels.size());
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        for (int i = 0; i < levels.size(); i++) {
            T value = levels.get(i).get(key, type);
            if (value != null) {
                log.debug("[{}] Cache hit(type) at L{}: key={}, type={}, levelClass={}", name, i + 1, key,
                    type.getSimpleName(), levels.get(i).getClass().getSimpleName());
                backfillUpperLevels(i, key, value);
                return value;
            }
        }
        log.debug("[{}] Cache miss(type) across all levels: key={}, type={}, levelCount={}", name, key,
            type.getSimpleName(), levels.size());
        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        int bottomIndex = levels.size() - 1;
        log.debug("[{}] Delegating to bottom layer L{} for valueLoader: key={}, bottomClass={}", name, bottomIndex + 1,
            key, levels.get(bottomIndex).getClass().getSimpleName());
        T value = levels.get(bottomIndex).get(key, valueLoader);
        if (value != null) {
            for (int i = bottomIndex - 1; i >= 0; i--) {
                backfillToLevel(i, key, value);
            }
        } else {
            log.debug("[{}] valueLoader returned null: key={}", name, key);
        }
        return value;
    }

    @Override
    public void put(Object key, Object value) {
        log.debug("[{}] Write-through to all levels: key={}, valueClass={}", name, key,
            value != null ? value.getClass().getSimpleName() : "null");
        for (int i = levels.size() - 1; i >= 0; i--) {
            try {
                levels.get(i).put(key, value);
                log.debug("[{}] Written to L{}: key={}, levelClass={}", name, i + 1, key,
                    levels.get(i).getClass().getSimpleName());
            } catch (Exception e) {
                log.error("[{}] Failed to write to L{}: key={}, levelClass={}, error={}", name, i + 1, key,
                    levels.get(i).getClass().getSimpleName(), e.getMessage(), e);
                throw e;
            }
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper existing = get(key);
        if (existing != null) {
            log.debug("[{}] putIfAbsent: key={} already exists, returning current value", name, key);
            return existing;
        }
        log.debug("[{}] putIfAbsent: key={} not found, writing to all levels", name, key);
        put(key, value);
        return null;
    }

    @Override
    public void evict(Object key) {
        log.debug("[{}] Evicting from all levels: key={}, levelCount={}", name, key, levels.size());
        for (int i = 0; i < levels.size(); i++) {
            Cache level = levels.get(i);
            try {
                level.evict(key);
                log.debug("[{}] Evicted from L{}: key={}, levelClass={}", name, i + 1, key,
                    level.getClass().getSimpleName());
            } catch (Exception e) {
                log.warn("[{}] Failed to evict from L{}: key={}, levelClass={}, error={}", name, i + 1, key,
                    level.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    @Override
    public void clear() {
        log.info("[{}] Clearing all levels: levelCount={}", name, levels.size());
        for (int i = 0; i < levels.size(); i++) {
            Cache level = levels.get(i);
            try {
                level.clear();
                log.debug("[{}] Cleared L{}: levelClass={}", name, i + 1, level.getClass().getSimpleName());
            } catch (Exception e) {
                log.warn("[{}] Failed to clear L{}: levelClass={}, error={}", name, i + 1,
                    level.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    /**
     * 回填上层缓存（静默，不触发监听器）
     *
     * @param hitIndex 命中的层级索引
     * @param key      缓存键
     * @param value    缓存值
     */
    protected void backfillUpperLevels(int hitIndex, Object key, Object value) {
        for (int i = hitIndex - 1; i >= 0; i--) {
            backfillToLevel(i, key, value);
        }
    }

    /**
     * 回填指定层级（静默）
     *
     * @param levelIndex 层级索引
     * @param key        缓存键
     * @param value      缓存值
     */
    protected void backfillToLevel(int levelIndex, Object key, Object value) {
        Cache level = levels.get(levelIndex);
        try {
            level.put(key, value);
            log.debug("[{}] Backfilled to L{}: key={}, levelClass={}", name, levelIndex + 1, key,
                level.getClass().getSimpleName());
        } catch (Exception e) {
            log.warn("[{}] Failed to backfill to L{}: key={}, levelClass={}, error={}", name, levelIndex + 1, key,
                level.getClass().getSimpleName(), e.getMessage());
        }
    }
}
