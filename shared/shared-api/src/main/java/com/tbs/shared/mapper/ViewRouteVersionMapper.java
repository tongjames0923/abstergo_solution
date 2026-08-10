package com.tbs.shared.mapper;

import com.tbs.shared.pojo.entity.ViewRouteVersionEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 视图路由版本数据访问接口
 *
 * @author abstergo
 */
@Repository
public interface ViewRouteVersionMapper extends IFrameworkMapper<ViewRouteVersionEntity, Long> {

    /**
     * 查询指定分组的版本历史（按版本序号倒序）
     *
     * @param routeGroup 路由分组
     * @return 版本记录列表（最新版本在前）
     */
    @Select("SELECT * FROM view_route_version WHERE route_group = #{routeGroup} ORDER BY version_no DESC")
    List<ViewRouteVersionEntity> findByGroupOrderByVersionNoDesc(@Param("routeGroup") String routeGroup);

    /**
     * 统计指定分组的版本数量
     *
     * @param routeGroup 路由分组
     * @return 版本数量
     */
    @Select("SELECT COUNT(*) FROM view_route_version WHERE route_group = #{routeGroup}")
    int countByGroup(@Param("routeGroup") String routeGroup);

    /**
     * 查询全局最大版本ID（即全局视图版本号）
     *
     * @return 最大版本ID，无记录时返回null
     */
    @Select("SELECT MAX(id) FROM view_route_version")
    Long findMaxId();

    /**
     * 查询所有版本记录（按ID倒序）
     * <p>
     * 用于启动时计算每个分组的当前版本（每组第一条即最新版本）
     * </p>
     *
     * @return 版本记录列表
     */
    @Select("SELECT * FROM view_route_version ORDER BY id DESC")
    List<ViewRouteVersionEntity> findAllOrderByIdDesc();
}
