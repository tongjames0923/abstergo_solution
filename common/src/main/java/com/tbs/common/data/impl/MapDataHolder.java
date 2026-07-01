package com.tbs.common.data.impl;

import com.tbs.common.data.IDataHolder;
import com.tbs.common.error.AssertUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于Map的数据存储实现类
 * <p>
 * 该类是IDataHolder接口的具体实现，使用Map作为底层数据存储结构。 提供了完整的数据存储功能，包括数据的增删改查、清空、状态检查等操作。
 * 同时继承了AbstractAntStyleMatchKeyFounder类，支持基于Ant风格路径匹配的键查找功能。 默认使用LinkedHashMap作为存储结构，保证插入顺序。
 *
 * @author abstergo
 * @see IDataHolder 数据存储接口
 * @see AbstractAntStyleMatchKeyFounder 抽象Ant风格键匹配器
 * @see Map 映射接口
 * @see LinkedHashMap 链式哈希映射实现
 */
public class MapDataHolder extends AbstractAntStyleMatchKeyFounder implements IDataHolder {

    /**
     * 底层数据存储Map
     * <p>
     * 用于存储键值对数据，支持任意类型的键和值。
     */
    private Map<String, Object> map;

    /**
     * 构造函数
     * <p>
     * 使用指定的Map作为底层数据存储结构。
     *
     * @param map 用于数据存储的Map实例，不能为null
     * @throws IllegalArgumentException 当map为null时抛出
     * @see AssertUtil 断言工具类
     */
    public MapDataHolder(Map<String, Object> map) {
        AssertUtil.notNull(map, "map is null");
        this.map = map;
    }

    /**
     * 默认构造函数
     * <p>
     * 创建一个新的ConcurrentHashMap实例作为底层数据存储结构，初始容量为8。
     *
     * @see ConcurrentHashMap 并发哈希映射实现
     */
    public MapDataHolder() {
        this.map = new ConcurrentHashMap<>(8);
    }

    /**
     * 存储数据
     * <p>
     * 实现IDataHolder接口的存储方法。将指定的值与键关联并存储到Map中。 如果force参数为true，则强制存储，即使该键已存在也会覆盖原有值； 如果force为false，则只有在键不存在时才存储。
     *
     * @param key   存储数据的键，不能为null
     * @param value 要存储的数据，可以为null
     * @param force 是否强制存储，true表示强制覆盖，false表示仅在键不存在时存储
     * @return 存储成功返回true，否则返回false
     * @see IDataHolder#set(String, Object, boolean) 数据存储接口方法
     * @see Map#put(Object, Object) Map存储方法
     * @see Map#containsKey(Object) Map键检查方法
     */
    @Override
    public boolean set(String key, Object value, boolean force) {
        if (force) {
            map.put(key, value);
            return true;
        }
        if (map.containsKey(key)) {
            return false;
        }
        map.put(key, value);
        return true;
    }

    /**
     * 获取数据
     * <p>
     * 实现IDataHolder接口的获取方法。根据指定的键从Map中获取对应的值， 以Optional形式返回，避免返回null值。如果键不存在或值为null，则返回空的Optional。
     *
     * @param key 数据键，不能为null
     * @param <T> 返回值的类型
     * @return 包含数据值的Optional对象，如果键不存在或值为null则为空
     * @see IDataHolder#get(String) 数据获取接口方法
     * @see Map#containsKey(Object) Map键检查方法
     * @see Map#get(Object) Map获取方法
     * @see Optional 可选值包装类
     */
    @Override
    public <T> Optional<T> get(String key) {
        if (map.containsKey(key)) {
            Object val = map.get(key);
            return Optional.ofNullable((T)val);
        }
        return Optional.empty();
    }

    /**
     * 删除数据
     * <p>
     * 实现IDataHolder接口的删除方法。根据指定的键从Map中删除对应的键值对数据。
     *
     * @param key 要删除数据的键，不能为null
     * @see IDataHolder#remove(String) 数据删除接口方法
     * @see Map#remove(Object) Map删除方法
     */
    @Override
    public void remove(String key) {
        map.remove(key);
    }

    /**
     * 清空数据
     * <p>
     * 实现IDataHolder接口的清空方法。删除Map中存储的所有数据，使其变为空状态。
     *
     * @see IDataHolder#clear() 数据清空接口方法
     * @see Map#clear() Map清空方法
     */
    @Override
    public void clear() {
        map.clear();
    }

    /**
     * 数据是否为空
     * <p>
     * 实现IDataHolder接口的空检查方法。检查Map是否不包含任何数据。
     *
     * @return 如果不包含任何数据返回true，否则返回false
     * @see IDataHolder#isEmpty() 数据空检查接口方法
     * @see Map#isEmpty() Map空检查方法
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * 数据大小
     * <p>
     * 实现IDataHolder接口的大小获取方法。返回Map中存储的键值对数量。
     *
     * @return 存储的键值对数量
     * @see IDataHolder#size() 数据大小接口方法
     * @see Map#size() Map大小方法
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * 数据是否包含某个key
     * <p>
     * 实现IDataHolder接口的键包含检查方法。检查Map是否包含指定的键。
     *
     * @param key 要检查的键，不能为null
     * @return 如果包含该键返回true，否则返回false
     * @see IDataHolder#containsKey(String) 键包含检查接口方法
     * @see Map#containsKey(Object) Map键包含检查方法
     */
    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    /**
     * 获取所有数据
     * <p>
     * 实现IDataHolder接口的条目集合获取方法。返回Map中存储的所有键值对条目集合。
     *
     * @return 包含所有键值对条目的集合
     * @see IDataHolder#entries() 条目集合获取接口方法
     * @see Map#entrySet() Map条目集合方法
     * @see Map.Entry 键值对条目接口
     * @see Collection 集合接口
     */
    @Override
    public Collection<Map.Entry<String, Object>> entries() {
        return map.entrySet();
    }

    /**
     * 获取所有key
     * <p>
     * 实现IDataHolder接口的键集合获取方法。返回Map中存储的所有键的集合。
     *
     * @return 包含所有键的集合
     * @see IDataHolder#keys() 键集合获取接口方法
     * @see Map#keySet() Map键集合方法
     * @see Collection 集合接口
     */
    @Override
    public Collection<String> keys() {
        return map.keySet();
    }

    /**
     * 获取所有value
     * <p>
     * 实现IDataHolder接口的值集合获取方法。返回Map中存储的所有值的集合。
     *
     * @return 包含所有值的集合
     * @see IDataHolder#values() 值集合获取接口方法
     * @see Map#values() Map值集合方法
     * @see Collection 集合接口
     */
    @Override
    public Collection<Object> values() {
        return map.values();
    }

    /**
     * 获取所有数据条目
     * <p>
     * 实现AbstractAntStyleMatchKeyFounder抽象方法。返回Map中存储的所有键值对条目集合， 用于支持Ant风格路径匹配的键查找功能。
     *
     * @return 包含所有键值对条目的集合
     * @see AbstractAntStyleMatchKeyFounder#getEntries() 抽象键匹配器方法
     * @see Map#entrySet() Map条目集合方法
     * @see Map.Entry 键值对条目接口
     * @see Collection 集合接口
     */
    @Override
    protected Collection<Map.Entry<String, Object>> getEntries() {
        return map.entrySet();
    }
}
