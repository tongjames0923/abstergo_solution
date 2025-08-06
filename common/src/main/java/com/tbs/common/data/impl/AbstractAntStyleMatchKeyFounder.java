package com.tbs.common.data.impl;

import com.tbs.common.data.IMatchKeyFounder;
import org.springframework.util.AntPathMatcher;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author abstergo
 */
public abstract class AbstractAntStyleMatchKeyFounder implements IMatchKeyFounder {
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 获取所有数据
     *
     * @return
     */
    protected abstract Collection<Map.Entry<String, Object>> getEntries();

    @Override
    public Collection<Map.Entry<String, Object>> findEntries(String key) {
        return getEntries().stream().filter(entry -> {
            return antPathMatcher.match(key, entry.getKey());
        }).collect(Collectors.toList());
    }
}
