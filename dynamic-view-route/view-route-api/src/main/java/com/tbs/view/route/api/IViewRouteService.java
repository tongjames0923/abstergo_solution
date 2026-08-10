package com.tbs.view.route.api;

import com.tbs.view.route.model.ViewRouteModel;
import com.tbs.view.route.model.ViewRouteVersionModel;

import java.util.List;

/**
 * 路由服务接口
 * <p>
 * 该接口定义了路由服务的基本操作，包括获取路由分组、获取分组下的路由信息、保存路由信息、删除路由信息、版本管理等
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
     * <p>
     * 保存会产生一条新的版本记录，历史版本不可变
     * </p>
     *
     * @param group          分组名称
     * @param viewRouteModel 路由信息列表
     * @param description    版本说明，可为空
     * @return 新产生的版本记录
     */
    ViewRouteVersionModel saveViewRoute(String group, List<ViewRouteModel> viewRouteModel, String description);

    /**
     * 删除路由信息
     * <p>
     * 删除分组即清空该分组的全部路由，同样以产生一条空版本记录的方式实现
     * </p>
     *
     * @param group 分组名称
     */
    void deleteViewRoute(String group);

    /**
     * 获取指定分组的版本历史
     *
     * @param group 分组名称
     * @return 版本记录列表（最新版本在前）
     */
    List<ViewRouteVersionModel> getVersionHistory(String group);

    /**
     * 回滚到指定版本
     * <p>
     * 复制目标历史版本的路由数据并产生一条新的版本记录，历史依然不可变
     * </p>
     *
     * @param group     分组名称
     * @param versionId 目标版本ID
     * @return 新产生的版本记录
     */
    ViewRouteVersionModel rollbackToVersion(String group, Long versionId);

    /**
     * 获取全局视图版本号
     * <p>
     * 即版本表的最大ID，单调递增、永不回退，用于前端版本比对
     * </p>
     *
     * @return 全局视图版本号，无记录时返回0
     */
    long getViewVersion();

}