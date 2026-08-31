package com.tbs.shared.mapper;

import com.tbs.shared.pojo.entity.SystemRoleGroupRelationEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 角色组关系Mapper
 *
 * @author abstergo
 */
@Repository
public interface SystemRoleGroupRelationMapper extends IFrameworkMapper<SystemRoleGroupRelationEntity, Long> {

    /**
     * 根据角色ID与角色组ID查询关系记录（包含已逻辑删除的记录）
     *
     * <p>绕过逻辑删除拦截器，用于追加角色时判断是否需重新激活已删除记录</p>
     *
     * @param roleId  角色ID
     * @param groupId 角色组ID
     * @return 关系记录，不存在时返回null
     */
    @Select("SELECT * FROM sys_role_group_relation WHERE role_id = #{roleId} AND group_id = #{groupId} LIMIT 1")
    SystemRoleGroupRelationEntity selectByRoleIdAndGroupIdIncludeDeleted(Long roleId, Long groupId);

}
