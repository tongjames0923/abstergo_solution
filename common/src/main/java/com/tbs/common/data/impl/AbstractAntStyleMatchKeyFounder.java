package com.tbs.common.data.impl;

import com.tbs.common.data.IMatchKeyFounder;
import org.springframework.util.AntPathMatcher;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于Ant风格路径匹配的抽象键查找器实现类
 * <p>
 * 该类是IMatchKeyFounder接口的抽象实现，使用Spring的AntPathMatcher来实现键的路径匹配功能。 支持Ant风格的路径匹配模式，如通配符(*)、多级通配符(**)等，可以方便地根据模式查找匹配的键值对。
 * 子类需要实现getEntries()抽象方法来提供所有键值对数据。
 *
 * @author abstergo
 * @see IMatchKeyFounder 键匹配器接口
 * @see AntPathMatcher Ant路径匹配器
 * @see Map.Entry 键值对条目接口
 * @see Collection 集合接口
 */
public abstract class AbstractAntStyleMatchKeyFounder implements IMatchKeyFounder {
    /**
     * Ant风格路径匹配器
     * <p>
     * 用于实现基于Ant风格的路径模式匹配，支持通配符(*)、多级通配符(**)等匹配模式。 例如：
     * <ul>
     * <li>"user.*" 可以匹配 "user.name"、"user.age" 等</li>
     * <li>"config.**" 可以匹配 "config" 以及 "config.db.url" 等多级路径</li>
     * </ul>
     *
     * @see AntPathMatcher Ant路径匹配器
     */
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 获取所有键值对条目
     * <p>
     * 抽象方法，由子类实现，用于提供所有需要进行匹配的键值对数据集合。
     *
     * @return 包含所有键值对条目的集合
     * @see Map.Entry 键值对条目接口
     * @see Collection 集合接口
     */
    protected abstract Collection<Map.Entry<String, Object>> getEntries();

    /**
     * 根据键模式查找匹配的键值对
     * <p>
     * 实现IMatchKeyFounder接口的核心方法，使用AntPathMatcher对所有键值对进行匹配过滤， 返回键名符合指定模式的键值对集合。
     *
     * @param key 键模式，使用Ant风格路径匹配模式，不能为null
     * @return 包含所有匹配键值对的集合
     * @see IMatchKeyFounder#findEntries(String) 键匹配器接口方法
     * @see AntPathMatcher#match(String, String) Ant路径匹配方法
     * @see Map.Entry 键值对条目接口
     * @see Collection 集合接口
     */
    @Override
    public Collection<Map.Entry<String, Object>> findEntries(String key) {
        return getEntries().stream().filter(entry -> {
            return antPathMatcher.match(key, entry.getKey());
        }).collect(Collectors.toList());
    }
}
