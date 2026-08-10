package com.tbs.shared.mapper;

import com.tbs.shared.pojo.entity.ViewRouteMetaEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 视图路由元数据数据访问接口
 *
 * @author abstergo
 */
@Repository
public interface ViewRouteMetaMapper extends IFrameworkMapper<ViewRouteMetaEntity, Long> {

    /**
     * 根据路由ID查询元数据
     *
     * @param routeId 路由ID
     * @return 元数据实体
     */
    @Select("SELECT * FROM view_route_meta WHERE route_id = #{routeId} AND flag = 1")
    ViewRouteMetaEntity findByRouteId(@Param("routeId") Long routeId);

    /**
     * 根据路由分组查询元数据列表
     *
     * @param routeGroup 路由分组
     * @return 元数据列表
     */
    @Select("SELECT * FROM view_route_meta WHERE route_group = #{routeGroup} AND flag = 1")
    List<ViewRouteMetaEntity> findByRouteGroup(@Param("routeGroup") String routeGroup);
}
