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

    @Select(
        "SELECT sd.* FROM sys_department sd RIGHT JOIN system_user_department_relation sudr ON sudr.user_id=#{userId}  AND sudr.department_id=sd.id;")
    List<SystemDepartmentEntity> queryUserDepartments(@NotNull Long userId);

    @Select(
        "SELECT * FROM sys_user su RIGHT JOIN system_user_department_relation sudr ON sudr.department_id=#{departmentId}  and sudr.user_id=su.id")
    List<SystemUserEntity> queryDepartmentUsers(@NotNull Long departmentId);

    @Select("SELECT * FROM system_user_department_relation WHERE user_id=#{userId} AND department_id=#{departmentId}")
    List<SystemUserDepartmentRelationEntity> queryUserDepartmentRelation(@NotNull Long userId, @NotNull Long departmentId);
}
