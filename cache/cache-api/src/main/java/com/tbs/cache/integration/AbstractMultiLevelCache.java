package com.tbs.cache.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 多层缓存编排抽象基类
 * <p>
 * 纯编排职责，不包含 TTL 能力（由装饰层 {@link AbstractAdaptTtlCache} 包装提供）。
 * 核心语义：
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
public abstract class AbstractMultiLevelCache implements Cache {

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
            throw new IllegalArgumentException("At least one cache level is required");
        }
        this.name = name;
        this.levels = Collections.unmodifiableList(new ArrayList<>(levels));
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
    public List<Cache> getLevels() {
        return levels;
    }

    @Override
    public ValueWrapper get(Object key) {
        // 按层级顺序穿透读取
        for (int i = 0; i < levels.size(); i++) {
            ValueWrapper value = levels.get(i).get(key);
            if (value != null) {
                // 命中，回填上层（静默，不触发监听器）
                backfillUpperLevels(i, key, value.get());
                return value;
            }
        }
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        for (int i = 0; i < levels.size(); i++) {
            T value = levels.get(i).get(key, type);
            if (value != null) {
                backfillUpperLevels(i, key, value);
                return value;
            }
        }
        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        // 委托最底层执行，复用其并发保护（如 Redisson 分布式锁）
        T value = levels.get(levels.size() - 1).get(key, valueLoader);
        if (value != null) {
            // 回填所有上层
            for (int i = levels.size() - 2; i >= 0; i--) {
                backfillToLevel(i, key, value);
            }
        }
        return value;
    }

    @Override
    public void put(Object key, Object value) {
        // 自底向上写入，远层失败快速失败，避免本地层污染
        for (int i = levels.size() - 1; i >= 0; i--) {
            levels.get(i).put(key, value);
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        // 先穿透检查，已存在则返回现值；不存在则全层写入
        // 注意：跨层级无法保证原子性，由装饰层的 valueLoader 并发保护兜底
        ValueWrapper existing = get(key);
        if (existing != null) {
            return existing;
        }
        put(key, value);
        return null;
    }

    @Override
    public void evict(Object key) {
        // 全层失效，尽力而为
        for (Cache level : levels) {
            try {
                level.evict(key);
            } catch (Exception e) {
                log.warn("[{}] Failed to evict from level {}: {}", name, level.getName(), e.getMessage());
            }
        }
    }

    @Override
    public void clear() {
        // 全层清空，尽力而为
        for (Cache level : levels) {
            try {
                level.clear();
            } catch (Exception e) {
                log.warn("[{}] Failed to clear level {}: {}", name, level.getName(), e.getMessage());
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
            log.debug("[{}] Backfilled to level {}: key={}", name, levelIndex, key);
        } catch (Exception e) {
            log.warn("[{}] Failed to backfill to level {}: {}", name, level.getName(), e.getMessage());
        }
    }
}
