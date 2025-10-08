package com.tbs.common.data;

/**
 * 数据仓库功能扩展接口
 * <p>
 * 该接口继承了IFeatureWrapper和IDataHolder两个接口，既是数据持有者(IDataHolder)的包装器，
 * 又具备数据持有者的所有功能。通过该接口可以对数据持有者进行功能扩展， 并提供了键名重写的能力，以支持更灵活的数据访问控制。
 *
 * @author abstergo
 * @see IFeatureWrapper 功能包装接口
 * @see IDataHolder 数据持有者接口
 */
public interface IPartitionableDataHolder extends IFeatureWrapper<IDataHolder>, IDataHolder {

	/**
	 * 重新生成键名
	 * <p>
	 * 对原始键名进行转换处理，生成新的键名。该方法可用于实现键名的前缀添加、 命名空间隔离、键名映射等高级功能。
	 *
	 * @param k 原始键名，不能为null
	 * @return 转换后的键名
	 */
	String reKey(String k);
}
