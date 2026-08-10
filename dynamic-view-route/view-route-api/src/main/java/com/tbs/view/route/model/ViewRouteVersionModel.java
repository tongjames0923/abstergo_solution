package com.tbs.view.route.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 视图路由版本模型类
 * <p>
 * 用于描述路由配置的一次版本记录，包含版本序号、版本说明、创建人与创建时间。
 * 每次保存/回滚/清空分组都会产生一条新的版本记录，历史不可变。
 * </p>
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ViewRouteVersionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 版本ID（全局唯一，单调递增，用作全局视图版本号）
     */
    private Long id;

    /**
     * 路由分组
     */
    private String routeGroup;

    /**
     * 组内版本序号（v1、v2...）
     */
    private Integer versionNo;

    /**
     * 版本说明
     */
    private String description;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;
}
