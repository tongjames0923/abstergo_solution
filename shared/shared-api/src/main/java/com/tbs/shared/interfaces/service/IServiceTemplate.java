package com.tbs.shared.interfaces.service;

/**
 * 标准的服务接口
 *
 * @param <E>
 * @param <K>
 * @author abstergo
 */
public interface IServiceTemplate<E, K>
		extends ISaveableService<E, K>, IDeleteableService<E, K>, ISearchableService<E, K> {
	/**
	 * 是否使用数据权限
	 *
	 * @return
	 */
	public default boolean useDataScope() {
		return false;
	}


}
