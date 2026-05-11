package com.tbs.app.service.department;

import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IDepartmentUserRelationService {

    /**
     * 查询用户所属部门
     *
     * @param userId
     * @return
     */
    List<SystemDepartmentEntity> queryUserDepartments(@NotNull Long userId);

    /**
     * 查询部门所属用户
     *
     * @param departmentId
     * @return
     */
    List<SystemUserEntity> queryDepartmentUsers(@NotNull Long departmentId);

    /**
     * 添加用户到部门
     *
     * @param userId
     * @param departmentId
     */
    void addUserToDepartment(@NotNull Long userId, @NotNull Long departmentId);

    /**
     * 删除用户从部门
     *
     * @param userId
     * @param departmentId
     */
    void deleteUserFromDepartment(@NotNull Long userId, @NotNull Long departmentId);
}
