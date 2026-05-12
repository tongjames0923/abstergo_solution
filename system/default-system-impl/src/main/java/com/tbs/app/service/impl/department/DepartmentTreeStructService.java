package com.tbs.app.service.impl.department;

import com.tbs.app.service.department.ISystemDepartmentService;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.shared.interfaces.service.AbstractTreeStructService;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.struct.tree.api.persistence.ITreeStore;
import org.apache.dubbo.config.annotation.DubboReference;

/**
 * 部门树形结构服务
 *
 * @author abstergo
 */
public class DepartmentTreeStructService extends AbstractTreeStructService<SystemDepartmentEntity> {

    @DubboReference
    private ITreeStore treeStore;

    @Override
    protected ITreeStore getTreeStore() {
        return treeStore;
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
