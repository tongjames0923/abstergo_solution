package com.tbs.app.service.department;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;

import java.util.List;

/**
 * 部门服务接口
 *
 * @author abstergo
 */
public interface ISystemDepartmentService extends IServiceTemplate<SystemDepartmentEntity, Long> {

    /**
     * 添加子部门
     *
     * @param parentId 父部门ID
     * @param childId  子部门ID
     */
    void appendChildDepartment(Long parentId, Long childId);

    /**
     * 获取下一级子部门列表
     *
     * @param id
     * @return
     */
    List<SystemDepartmentEntity> getChildrenDepartments(Long id);

}
