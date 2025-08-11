package com.tbs.common.data.impl;

import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderFeatureWrapper;
import com.tbs.common.data.IMatchKeyFounder;
import com.tbs.common.error.AssertUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * 默认自动生成数据持有者管理器实现类
 * <p>
 * 该类是AbstractAutoGenDataHolderManager的默认实现，用于自动创建和管理分区数据持有者实例。
 * 每个分区数据持有者都基于全局数据持有者，并通过键名前缀的方式实现数据分区隔离， 防止不同分区之间的数据冲突。分区键名格式为"分区名::原始键名"。
 *
 * @author abstergo
 * @see AbstractAutoGenDataHolderManager 抽象自动生成数据持有者管理器
 * @see IDataHolder 数据持有者接口
 * @see PartitionDataHolder 分区数据持有者内部类
 */
public class DefaultAutoGenDataHodlerManager extends AbstractAutoGenDataHolderManager {

	/**
	 * 获取部分数据持有者
	 * <p>
	 * 实现AbstractAutoGenDataHolderManager的抽象方法，用于创建分区数据持有者实例。
	 * 每个分区数据持有者都包装了全局数据持有者，并通过键名前缀实现数据分区隔离。
	 *
	 * @param name            数据持有者名称，即分区名称
	 * @param gobalDataHolder 全局数据持有者，作为数据源
	 * @return 新创建的分区数据持有者实例
	 * @see AbstractAutoGenDataHolderManager#getPartOfDataHolder(String,
	 *      IDataHolder) 抽象方法
	 * @see PartitionDataHolder 分区数据持有者内部类
	 */
	@Override
	protected IDataHolder getPartOfDataHolder(String name, IDataHolder gobalDataHolder) {
		return new PartitionDataHolder(name, gobalDataHolder);
	}

	/**
	 * 分区数据持有者实现类
	 * <p>
	 * 该类实现了IDataHolderFeatureWrapper接口，既是数据持有者又具备包装器功能，
	 * 通过键名前缀的方式实现数据分区隔离。每个分区数据持有者都关联一个分区名称， 在存储和读取数据时会自动为键名添加分区前缀，格式为"分区名::原始键名"。
	 *
	 * @see IDataHolderFeatureWrapper 数据持有者功能包装器接口
	 * @see IDataHolder 数据持有者接口
	 * @see IMatchKeyFounder 键匹配器接口
	 */
	public static class PartitionDataHolder implements IDataHolderFeatureWrapper {

		/**
		 * 分区名称
		 * <p>
		 * 用于为键名添加前缀，实现数据分区隔离。分区名称不可为null或空字符串， 在构造函数中进行初始化并可通过getter/setter方法访问和修改。
		 */
		@Getter
		@Setter
		private String partitionName;

		/**
		 * 被包装的全局数据持有者
		 * <p>
		 * 实际的数据存储载体，所有数据最终都存储在此数据持有者中， 通过键名前缀实现逻辑上的数据分区隔离。
		 */
		private IDataHolder dataHolder;

		/**
		 * 构造函数
		 *
		 * @param partitionName 分区名称，不能为null
		 * @param dataHolder    全局数据持有者，不能为null
		 */
		public PartitionDataHolder(String partitionName, IDataHolder dataHolder) {
			this.partitionName = partitionName;
			this.dataHolder = dataHolder;
		}

		/**
		 * 获取键匹配器
		 * <p>
		 * 检查被包装的数据持有者是否支持键匹配功能，如果支持则返回IMatchKeyFounder实例，
		 * 否则抛出运行时异常。键匹配功能用于支持通配符匹配等高级键查找操作。
		 *
		 * @return 键匹配器实例
		 * @throws RuntimeException 当被包装的数据持有者不支持键匹配功能时抛出
		 * @see IMatchKeyFounder 键匹配器接口
		 * @see AssertUtil 断言工具类
		 */
		private IMatchKeyFounder getMatch() {
			AssertUtil.isTrue(!(getOrginal() instanceof IMatchKeyFounder), () -> new RuntimeException("数据缓存不支持匹配键"));
			return (IMatchKeyFounder) dataHolder;
		}

		/**
		 * 生成带前缀的键名
		 * <p>
		 * 将原始键名转换为带分区前缀的键名，格式为"分区名::原始键名"， 实现数据的分区隔离，防止不同分区之间的键名冲突。
		 *
		 * @param key 原始键名，不能为null
		 * @return 带分区前缀的键名
		 */
		private String keyGen(String key) {
			return partitionName + "::" + key;
		}

		/**
		 * 存储数据
		 * <p>
		 * 实现IDataHolder接口的存储方法。将指定的值与键关联并存储， 键名会自动添加分区前缀以实现数据分区隔离。如果force参数为true，则强制存储，
		 * 即使该键已存在也会覆盖原有值；如果force为false，则只有在键不存在时才存储。
		 *
		 * @param key   存储数据的键，不能为null
		 * @param value 要存储的数据，可以为null
		 * @param force 是否强制存储，true表示强制覆盖，false表示仅在键不存在时存储
		 * @return 存储成功返回true，否则返回false
		 * @see IDataHolder#set(String, Object, boolean) 数据持有者存储方法
		 */
		@Override
		public boolean set(String key, Object value, boolean force) {
			return dataHolder.set(keyGen(key), value, force);
		}

		/**
		 * 获取数据
		 * <p>
		 * 实现IDataHolder接口的获取方法。根据指定的键获取对应的值，以Optional形式返回，
		 * 避免返回null值。键名会自动添加分区前缀以实现数据分区隔离查找。如果键不存在或值为null，则返回空的Optional。
		 *
		 * @param key 数据键，不能为null
		 * @param <T> 返回值的类型
		 * @return 包含数据值的Optional对象，如果键不存在或值为null则为空
		 * @see IDataHolder#get(String) 数据持有者获取方法
		 * @see Optional 可选值包装类
		 */
		@Override
		public <T> Optional<T> get(String key) {
			return getOrginal().get(keyGen(key));
		}

		/**
		 * 删除数据
		 * <p>
		 * 实现IDataHolder接口的删除方法。根据指定的键删除对应的键值对数据， 键名会自动添加分区前缀以实现数据分区隔离删除操作。
		 *
		 * @param key 要删除数据的键，不能为null
		 * @see IDataHolder#remove(String) 数据持有者删除方法
		 */
		@Override
		public void remove(String key) {
			getOrginal().remove(keyGen(key));
		}

		/**
		 * 清空数据
		 * <p>
		 * 实现IDataHolder接口的清空方法。删除当前分区下所有存储的数据， 通过匹配"分区名::*"模式的键来定位需要删除的数据项。
		 *
		 * @see IDataHolder#clear() 数据持有者清空方法
		 * @see IMatchKeyFounder#find(String) 键匹配器查找方法
		 */
		@Override
		public void clear() {
			getMatch().find(keyGen("*")).forEach(key -> dataHolder.remove(key));
		}

		/**
		 * 数据是否为空
		 * <p>
		 * 实现IDataHolder接口的空检查方法。检查当前分区是否不包含任何数据， 通过匹配"分区名::*"模式的键来判断分区数据是否为空。
		 *
		 * @return 如果当前分区不包含任何数据返回true，否则返回false
		 * @see IDataHolder#isEmpty() 数据持有者空检查方法
		 * @see IMatchKeyFounder#find(String) 键匹配器查找方法
		 */
		@Override
		public boolean isEmpty() {
			return getMatch().find(keyGen("*")).isEmpty();
		}

		/**
		 * 数据大小
		 * <p>
		 * 实现IDataHolder接口的大小获取方法。返回当前分区中存储的键值对数量， 通过匹配"分区名::*"模式的键来统计分区数据的数量。
		 *
		 * @return 当前分区中存储的键值对数量
		 * @see IDataHolder#size() 数据持有者大小方法
		 * @see IMatchKeyFounder#find(String) 键匹配器查找方法
		 */
		@Override
		public int size() {
			return getMatch().find(keyGen("*")).size();
		}

		/**
		 * 数据是否包含某个key
		 * <p>
		 * 实现IDataHolder接口的键包含检查方法。检查当前分区是否包含指定的键， 键名会自动添加分区前缀以实现数据分区隔离检查操作。
		 *
		 * @param key 要检查的键，不能为null
		 * @return 如果当前分区包含该键返回true，否则返回false
		 * @see IDataHolder#containsKey(String) 数据持有者键包含检查方法
		 */
		@Override
		public boolean containsKey(String key) {
			return getOrginal().containsKey(keyGen(key));
		}

		/**
		 * 获取所有数据
		 * <p>
		 * 实现IDataHolder接口的条目集合获取方法。返回当前分区中存储的所有键值对条目集合， 通过匹配"分区名::*"模式的键来定位当前分区的所有数据项。
		 *
		 * @return 包含当前分区所有键值对条目的集合
		 * @see IDataHolder#entries() 数据持有者条目集合获取方法
		 * @see IMatchKeyFounder#findEntries(String) 键匹配器条目查找方法
		 * @see Map.Entry 键值对条目接口
		 */
		@Override
		public Collection<Map.Entry<String, Object>> entries() {
			return getMatch().findEntries(keyGen("*"));
		}

		/**
		 * 获取所有key
		 * <p>
		 * 实现IDataHolder接口的键集合获取方法。返回当前分区中存储的所有键的集合， 通过匹配"分区名::*"模式的键来定位当前分区的所有键名。
		 *
		 * @return 包含当前分区所有键的集合
		 * @see IDataHolder#keys() 数据持有者键集合获取方法
		 * @see IMatchKeyFounder#find(String) 键匹配器查找方法
		 * @see Collection 集合接口
		 */
		@Override
		public Collection<String> keys() {
			return getMatch().find(keyGen("*"));
		}

		/**
		 * 获取所有value
		 * <p>
		 * 实现IDataHolder接口的值集合获取方法。返回当前分区中存储的所有值的集合， 通过匹配"分区名::*"模式的键来定位当前分区的所有值。
		 *
		 * @return 包含当前分区所有值的集合
		 * @see IDataHolder#values() 数据持有者值集合获取方法
		 * @see IMatchKeyFounder#findValues(String) 键匹配器值查找方法
		 * @see Collection 集合接口
		 */
		@Override
		public Collection<Object> values() {
			return getMatch().findValues(keyGen("*"));
		}

		/**
		 * 获取包装的原始对象
		 * <p>
		 * 实现IFeatureWrapper接口的获取原始对象方法。返回被当前包装器包装的原始数据持有者实例， 即全局数据持有者实例，用于直接访问原始数据存储结构。
		 *
		 * @return 被包装的原始数据持有者实例
		 * @see IFeatureWrapper#getOrginal() 功能包装器获取原始对象方法
		 * @see IDataHolder 数据持有者接口
		 */
		@Override
		public IDataHolder getOrginal() {
			return dataHolder;
		}

		/**
		 * 设置包装的原始对象
		 * <p>
		 * 实现IFeatureWrapper接口的设置原始对象方法。更新当前包装器包装的原始数据持有者实例， 即更新全局数据持有者实例，用于替换原始数据存储结构。
		 *
		 * @param orginal 要设置的原始数据持有者实例，不能为null
		 * @see IFeatureWrapper#setOrginal(Object) 功能包装器设置原始对象方法
		 * @see IDataHolder 数据持有者接口
		 */
		@Override
		public void setOrginal(IDataHolder orginal) {
			this.dataHolder = orginal;
		}

		/**
		 * 重新生成键名
		 * <p>
		 * 实现IDataHolderFeatureWrapper接口的键名重写方法。对原始键名进行转换处理，
		 * 为其添加当前分区的前缀，生成新的键名。该方法用于实现键名的分区隔离功能， 格式为"分区名::原始键名"。
		 *
		 * @param k 原始键名，不能为null
		 * @return 添加了分区前缀的新键名
		 * @see IDataHolderFeatureWrapper#reKey(String) 数据持有者功能包装器键名重写方法
		 */
		@Override
		public String reKey(String k) {
			return keyGen(k);
		}
	}
}
