package com.tbs.view.route.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collection;

/**
 * 视图路由元数据模型类 用于定义视图路由的元数据信息，包括标题、图标、权限标识等
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewRouteMetaModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 路由分组
     */
    private String routeGroup;

    /**
     * 路由标题
     */
    private String title;
    /**
     * 路由图标
     */
    private String icon;

    /**
     * 路由权限标识
     */
    private Collection<String> roles;

    /**
     * 是否显示面包屑
     */
    private Boolean breadcrumb;

    /**
     * 当前激活的菜单
     */
    private String activeMenu;
}
