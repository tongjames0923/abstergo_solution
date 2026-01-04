package com.tbs.script.model;

import com.tbs.common.data.structs.KeyValuePair;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.base.StructUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 脚本编译器执行特性
 *
 * @author abstergo
 */
public class ScriptCompilerFeatures {
    private Map<String, Object> features = new HashMap<>(32);

    public ScriptCompilerFeatures() {
    }

    /**
     * 添加脚本编译器特性
     *
     * @param features 脚本编译器特性
     */
    private void listFeaturesInsert(Collection<KeyValuePair<String, Integer>> features) {
        if (CollUtil.isEmpty(features)) {
            return;
        }
        for (KeyValuePair<String, Integer> pair : features) {
            if (pair == null) {
                continue;
            }
            if (StrUtil.isEmpty(pair.getKey())) {
                continue;
            }
            if (pair.getValue() == null) {
                continue;
            }
            ScriptCompilerFeatures.this.putFeatureCode(pair.getKey(), pair.getValue());

        }
    }

    /**
     * 构造函数
     *
     * @param features 脚本编译器特性
     */
    public ScriptCompilerFeatures(Collection<KeyValuePair<String, Integer>> features) {
        listFeaturesInsert(features);
    }

    /**
     * 构造函数
     *
     * @param pairs 脚本编译器特性
     */
    public ScriptCompilerFeatures(KeyValuePair<String, Integer>... pairs) {
        listFeaturesInsert(Arrays.asList(pairs));
    }

    /**
     * 获取脚本编译器特性代码
     *
     * @param featureName 特性名称
     * @return 特性代码
     */
    public Optional<Object> getFeatureCode(String featureName) {
        return Optional.ofNullable(features.get(featureName));
    }

    /**
     * 添加/更新脚本编译器特性
     *
     * @param featureName 特性名称
     * @param featureCode 特性代码
     */
    public ScriptCompilerFeatures putFeatureCode(String featureName, Object value) {
        features.put(featureName, value);
        return this;
    }

    /**
     * 添加/更新脚本编译器特性，如果特性已存在则不更新
     *
     * @param featureName 特性名称
     * @param featureCode 特性代码
     */
    public ScriptCompilerFeatures putFeatureCodeIfAbsent(String featureName, Object value) {
        features.putIfAbsent(featureName, value);
        return this;
    }

    /**
     * 获取脚本编译器特性名称列表
     *
     * @return 特性名称列表
     */
    public List<String> getFeatureNames() {
        return features.keySet().stream().collect(Collectors.toList());
    }

    /**
     * 获取脚本编译器特性列表
     *
     * @return 脚本编译器特性列表
     */
    public Collection<KeyValuePair<String, Object>> getFeatures() {
        return features.entrySet().stream().map(StructUtils::mapEntryToTuple).collect(Collectors.toList());
    }

}
