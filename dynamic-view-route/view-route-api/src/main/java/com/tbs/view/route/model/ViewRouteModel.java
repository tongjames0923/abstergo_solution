package com.tbs.view.route.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;

/**
 * 视图路由模型类 用于定义前端路由信息，包括路径、组件、重定向等配置
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ViewRouteModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 对应的组件名称或路径
     */
    private String component;

    /**
     * 重定向路径
     */
    private String redirect;

    /**
     * 路由名称
     */
    private String name;

    /**
     * 是否隐藏路由
     */
    private Boolean hidden;

    /**
     * 是否始终显示路由
     */
    private Boolean alwaysShow;

    /**
     * 子路由
     */
    private Collection<ViewRouteModel> children;

    /**
     * 路由元数据
     */
    private ViewRouteMetaModel meta;

    /**
     * 排序顺序
     */
    private Integer sortOrder;
}
