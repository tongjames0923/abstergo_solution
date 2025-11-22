package com.tbs.view.route.api;

import com.tbs.view.route.model.ViewRouteModel;

import java.util.List;

/**
 * 路由服务接口
 * <p>
 * 该接口定义了路由服务的基本操作，包括获取路由分组、获取分组下的路由信息、保存路由信息、删除路由信息等
 * </p>
 * @author tongj
 */
public interface IViewRouteService {

    /**
     * 获取所有可见的路由分组
     *
     * @return 路由分组列表
     */
    List<String> getAllViewRouteGroups();

    /**
     * 获取指定分组下的所有可见的路由信息
     *
     * @param group 分组名称
     * @return 路由信息列表
     */
    List<ViewRouteModel> getViewRoutesByGroup(String group);

    /**
     * 保存路由信息
     *
     * @param group              分组名称
     * @param viewRouteModel 路由信息列表
     */
    void saveViewRoute(String group, List<ViewRouteModel> viewRouteModel);

    /**
     * 删除路由信息
     *
     * @param group 分组名称
     */
    void deleteViewRoute(String group);

}