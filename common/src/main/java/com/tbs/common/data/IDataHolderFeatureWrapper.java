package com.tbs.common.data;

/**
 * 数据仓库功能扩展接口
 *
 * @author abstergo
 */
public interface IDataHolderFeatureWrapper extends IFeatureWrapper<IDataHolder>, IDataHolder {

    /**
     * 重新生成key
     *
     * @param k
     * @return
     */
    String reKey(String k);
}
