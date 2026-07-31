package com.tbs.shared.pojo;

/**
 * 部门关联接口
 *
 * @author abstergo
 */
public interface IDeptBelongEntity<K> {
    /**
     * 获取部门ID
     *
     * @return
     */
    K getDeptId();

    /**
     * 设置部门ID
     *
     * @param deptId
     */
    void setDeptId(K deptId);
}
