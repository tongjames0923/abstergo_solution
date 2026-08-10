package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 视图路由配置实体
 * <p>
 * 用于存储动态路由配置信息，支持从数据库加载路由配置
 * </p>
 * 
 * @author abstergo
 */
@Entity
@Table(name = "view_route_configration", indexes = {
    @javax.persistence.Index(name = "idx_route_group", columnList = "route_group"),
    @javax.persistence.Index(name = "idx_parent_id", columnList = "parent_id"),
    @javax.persistence.Index(name = "idx_flag", columnList = "flag"),
    @javax.persistence.Index(name = "idx_sort_order", columnList = "sort_order")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewRouteConfigrationEntity implements IBaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 路由分组（如 common, admin, develop）
     */
    @Column(name = "route_group", length = 64, nullable = false)
    private String routeGroup;

    /**
     * 路由路径
     */
    @Column(name = "path", length = 255, nullable = false)
    private String path;

    /**
     * 组件路径
     */
    @Column(name = "component", length = 255)
    private String component;

    /**
     * 路由名称
     */
    @Column(name = "name", length = 128)
    private String name;

    /**
     * 重定向路径
     */
    @Column(name = "redirect", length = 255)
    private String redirect;

    /**
     * 是否隐藏（0:否，1:是）
     */
    @Column(name = "hidden")
    private Integer hidden;

    /**
     * 是否始终显示（0:否，1:是）
     */
    @Column(name = "always_show")
    private Integer alwaysShow;

    /**
     * 父路由ID（用于构建树形结构）
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 排序顺序
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 状态标志（0:禁用，1:启用）
     */
    @Column(name = "flag")
    private Integer flag;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 创建人
     */
    @Column(name = "create_by")
    private Long createBy;

    /**
     * 更新人
     */
    @Column(name = "update_by")
    private Long updateBy;
}