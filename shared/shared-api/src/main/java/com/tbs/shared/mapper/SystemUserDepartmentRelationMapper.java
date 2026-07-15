package com.tbs.shared.mapper;

import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.shared.pojo.entity.SystemUserDepartmentRelationEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Select;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 系统用户部门关系Mapper
 */
@Repository
public interface SystemUserDepartmentRelationMapper extends IFrameworkMapper<SystemUserDepartmentRelationEntity, Long> {

    /**
     * 查询部门用户
     *
     * @param departmentId 部门ID
     * @return 用户列表
     */
    @Select(
        "SELECT su.* FROM sys_user su INNER JOIN system_user_department_relation sudr ON sudr.department_id=#{departmentId} AND sudr.user_id=su.id WHERE su.id IS NOT NULL;")
    List<SystemUserEntity> queryDepartmentUsers(@NotNull Long departmentId);

    /**
     * 查询用户部门关系
     *
     * @param userId       用户ID
     * @param departmentId 部门ID
     * @return 部门关系列表
     */
    @Select("SELECT * FROM system_user_department_relation WHERE user_id=#{userId} AND department_id=#{departmentId}")
    List<SystemUserDepartmentRelationEntity> queryUserDepartmentRelation(@NotNull Long userId, @NotNull Long departmentId);
}
