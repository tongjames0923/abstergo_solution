package com.tbs.common.data;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 缓存键匹配器接口
 * <p>
 * 该接口定义了根据指定键模式查找匹配的缓存键、值或键值对的功能。
 * 提供了默认实现方法用于简化常见的查找操作，并定义了一个抽象方法用于具体的键值对匹配逻辑实现。
 *
 * @author tongj
 */
public interface IMatchKeyFounder {
	/**
	 * 匹配缓存键
	 * <p>
	 * 根据指定的键模式查找所有匹配的缓存键。该方法是默认实现，通过调用 {@link #findEntries(String)}
	 * 方法获取匹配的键值对，然后提取其中的键。
	 *
	 * @param key 键模式，用于匹配缓存中的键
	 * @return 包含所有匹配键的集合
	 * @see #findEntries(String) 获取匹配的键值对
	 * @see Map.Entry 键值对条目
	 */
	default Collection<String> find(String key) {
		return findEntries(key).stream().map(Map.Entry::getKey).collect(Collectors.toList());
	}

	/**
	 * 匹配缓存键对应的值
	 * <p>
	 * 根据指定的键模式查找所有匹配的缓存键对应的值。该方法是默认实现，通过调用 {@link #findEntries(String)}
	 * 方法获取匹配的键值对，然后提取其中的值。
	 *
	 * @param key 键模式，用于匹配缓存中的键
	 * @return 包含所有匹配值的集合
	 * @see #findEntries(String) 获取匹配的键值对
	 * @see Map.Entry 键值对条目
	 */
	default Collection<Object> findValues(String key) {
		return findEntries(key).stream().map(Map.Entry::getValue).collect(Collectors.toList());
	}

	/**
	 * 匹配缓存键对应的键值对
	 * <p>
	 * 根据指定的键模式查找所有匹配的缓存键值对。这是核心抽象方法，需要实现类提供具体实现逻辑。
	 *
	 * @param key 键模式，用于匹配缓存中的键值对
	 * @return 包含所有匹配键值对的集合
	 * @see Map.Entry 键值对条目
	 */
	Collection<Map.Entry<String, Object>> findEntries(String key);
}
