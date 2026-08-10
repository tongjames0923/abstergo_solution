package com.tbs.shared.mapper;

import com.tbs.shared.pojo.entity.ViewRouteRoleEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 视图路由权限数据访问接口
 *
 * @author abstergo
 */
@Repository
public interface ViewRouteRoleMapper extends IFrameworkMapper<ViewRouteRoleEntity, Long> {

    /**
     * 根据路由ID查询权限列表
     *
     * @param routeId 路由ID
     * @return 权限列表
     */
    @Select("SELECT * FROM view_route_role WHERE route_id = #{routeId} AND flag = 1")
    List<ViewRouteRoleEntity> findByRouteId(@Param("routeId") Long routeId);

    /**
     * 根据权限编码查询路由ID列表
     *
     * @param roleCode 权限编码
     * @return 路由ID列表
     */
    @Select("SELECT route_id FROM view_route_role WHERE role_code = #{roleCode} AND flag = 1")
    List<Long> findRouteIdsByRoleCode(@Param("roleCode") String roleCode);
}
