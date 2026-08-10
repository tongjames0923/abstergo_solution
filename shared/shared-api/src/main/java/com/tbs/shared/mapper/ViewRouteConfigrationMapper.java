package com.tbs.shared.mapper;

import com.tbs.shared.pojo.entity.ViewRouteConfigrationEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 视图路由配置数据访问接口
 *
 * @author abstergo
 */
@Repository
public interface ViewRouteConfigrationMapper extends IFrameworkMapper<ViewRouteConfigrationEntity, Long> {

    /**
     * 根据路由分组查询路由配置
     *
     * @param routeGroup 路由分组
     * @return 路由配置列表
     */
    @Select("SELECT * FROM view_route_configration WHERE route_group = #{routeGroup} AND flag = 1 ORDER BY sort_order")
    List<ViewRouteConfigrationEntity> findByRouteGroup(@Param("routeGroup") String routeGroup);

    /**
     * 根据父路由ID查询子路由
     *
     * @param parentId 父路由ID
     * @return 子路由列表
     */
    @Select("SELECT * FROM view_route_configration WHERE parent_id = #{parentId} AND flag = 1 ORDER BY sort_order")
    List<ViewRouteConfigrationEntity> findByParentId(@Param("parentId") Long parentId);

    /**
     * 查询所有启用的路由配置
     *
     * @return 路由配置列表
     */
    @Select("SELECT * FROM view_route_configration WHERE flag = 1 ORDER BY route_group, sort_order")
    List<ViewRouteConfigrationEntity> findAllEnabled();

    /**
     * 根据版本ID查询该版本下的所有路由配置
     *
     * @param versionId 版本ID
     * @return 路由配置列表
     */
    @Select("SELECT * FROM view_route_configration WHERE version = #{versionId} AND flag = 1 ORDER BY sort_order")
    List<ViewRouteConfigrationEntity> findByVersionId(@Param("versionId") Long versionId);
}
