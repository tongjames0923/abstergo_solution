package com.tbs.app.service.impl.department;

import com.tbs.app.service.department.ISystemDepartmentService;
import com.tbs.common.provider.IObjectProvider;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.shared.interfaces.service.AbstractTreeStructService;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.struct.tree.api.persistence.ITreeStore;

import jakarta.annotation.Resource;

/**
 * 部门树形结构服务
 * <p>
 * 通过 IObjectProvider 解析 ITreeStore：本地有 Bean 时直取（零协议开销），
 * 本地无 Bean 时自动降级 Dubbo 远程引用。
 * </p>
 *
 * @author abstergo
 */
public class DepartmentTreeStructService extends AbstractTreeStructService<SystemDepartmentEntity> {

    @Resource
    private IObjectProvider objectProvider;

    @Override
    @SuppressWarnings("unchecked")
    protected ITreeStore<Long> getTreeStore() {
        return objectProvider.getObject(ITreeStore.class);
    }

    private static final String DEPARTMENT_TREE_GROUP = "DEPARTMENT_TREE";

    @Override
    protected String getGroupName() {
        return DEPARTMENT_TREE_GROUP;
    }

    @Override
    protected String getNodeId(Long d, SystemDepartmentEntity systemDepartmentEntity) {
        return DEPARTMENT_TREE_GROUP + "::" + String.valueOf(d);
    }

    @Override
    protected SystemDepartmentEntity queryById(Long aLong) {
        return SpringUtil.getBean(ISystemDepartmentService.class).queryById(aLong);
    }
}
