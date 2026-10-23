package com.tbs.common.registry;

import java.util.function.BiPredicate;

/**
 * 键匹配策略契约。
 * <p>
 * 决定「已注册键」与「查询键」是否视为命中，是注册表从精确匹配走向通配/谓词匹配的扩展位。
 * 本期仅落地 {@link #EXACT}（equals 精确匹配），通配（如 topic {@code *} / {@code #}）与谓词匹配
 * 作为后续扩展预留、不接线，待出现第 2 个同构需求再实现，避免过度设计。
 * </p>
 *
 * @param <K> 键类型
 * @author abstergo
 * @since 1.0.0
 */
public interface IKeyMatcher<K> extends BiPredicate<K, K> {

    /**
     * 精确匹配：注册键 equals 查询键
     */
    IKeyMatcher<Object> EXACT = Object::equals;

    /**
     * 判断查询键是否命中注册键
     *
     * @param registeredKey 已注册键，不能为null
     * @param queryKey      查询键，不能为null
     * @return true 表示该注册键对本次查询命中
     */
    @Override
    boolean test(K registeredKey, K queryKey);
}
